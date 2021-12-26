package com.wx.wiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wx.wiki.domain.User;
import com.wx.wiki.domain.UserExample;
import com.wx.wiki.exception.BusinessException;
import com.wx.wiki.exception.BusinessExceptionCode;
import com.wx.wiki.mapper.UserMapper;
import com.wx.wiki.req.UserLoginReq;
import com.wx.wiki.req.UserQueryReq;
import com.wx.wiki.req.UserResetPassword;
import com.wx.wiki.req.UserSaveReq;
import com.wx.wiki.resp.UserLoginResp;
import com.wx.wiki.resp.UserQueryResp;
import com.wx.wiki.resp.PageResp;
import com.wx.wiki.util.CopyUtil;
import com.wx.wiki.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Resource
    private UserMapper userMapper;

    @Resource
    private SnowFlake snowFlake;

    public PageResp<UserQueryResp> list(UserQueryReq req) {

        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        // 参数可以为空
        if (!ObjectUtils.isEmpty(req.getLoginName())) {
            criteria.andNameLike("%" + req.getLoginName() + "%");
        }
        // 只对第一个select有用 所以最好就把这2个放在一起
        PageHelper.startPage(req.getPage(), req.getSize());
        List<User> userList = userMapper.selectByExample(userExample);

        PageInfo<User> pageInfo = new PageInfo<>(userList);
        LOG.info("总行数: {}", pageInfo.getTotal());
        LOG.info("总页数: {}", pageInfo.getPages());

        // 列表复制 => 工具类的列表复制
        List<UserQueryResp> list = CopyUtil.copyList(userList, UserQueryResp.class);

        PageResp<UserQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);

        return pageResp;
    }

    /**
     * save 既要支持新增也要支持更新
     */
    public void save(UserSaveReq req) {
        User user = CopyUtil.copy(req, User.class);
        if (ObjectUtils.isEmpty(req.getId())) {
            if (ObjectUtils.isEmpty(selectByLoginName(req.getLoginName()))) {
                // 新增
                user.setId(snowFlake.nextId());
                userMapper.insert(user);
            } else {
                // 提示用户名已存在
                throw new BusinessException(BusinessExceptionCode.USER_LOGIN_NAME_EXIST);
            }
        } else {
            // 更新
            user.setLoginName(null); // 设置为空，不更新登录名，不让用户进行编辑
            user.setPassword(null); // 修改的时候也不修改密码
            userMapper.updateByPrimaryKeySelective(user); // 字段有值才会更新
        }
    }

    /**
     * 删除电子书
     *
     * @param id Long
     */
    public void delete(Long id) {
        userMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据登录名查询用户信息
     * @param loginName
     * @return
     */
    public User selectByLoginName(String loginName) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andLoginNameEqualTo(loginName);
        List<User> userList = userMapper.selectByExample(userExample);
        if (CollectionUtils.isEmpty(userList)) {
            return null;
        } else {
            return userList.get(0);
        }
    }

    // 修改密码
    public void resetPassword(UserResetPassword req) {
        // id 和 password 有值
        User user = CopyUtil.copy(req, User.class);
        userMapper.updateByPrimaryKeySelective(user);
    }

    // 登录
    public UserLoginResp login(UserLoginReq req) {
        // 校验 按用户名去数据库查
        User userDB = selectByLoginName(req.getLoginName());
        if (ObjectUtils.isEmpty(userDB)) {
            // 用户名不存在
            LOG.info("用户名不存在: {}", req.getLoginName()); // 日志记录一下
            // 提示注意 用户名不存在或密码不对，干扰攻击者的判断 一个模糊的提示
            throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
        } else {
            // 比对密码
            if (userDB.getPassword().equals(req.getPassword())) {
                // 登录成功
                UserLoginResp userLoginResp = CopyUtil.copy(userDB, UserLoginResp.class);
                return userLoginResp;
            } else {
                // 密码不正确
                // 可以尝试加一个功能：输入错误几次以上就锁定用户
                LOG.info("密码不对, 输入密码: {}, 数据库密码: {}", req.getLoginName(), userDB.getPassword()); // 日志记录一下
                throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
            }
        }
    }
}
