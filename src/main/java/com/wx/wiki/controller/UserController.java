package com.wx.wiki.controller;

import com.alibaba.fastjson.JSONObject;
import com.wx.wiki.req.UserLoginReq;
import com.wx.wiki.req.UserQueryReq;
import com.wx.wiki.req.UserResetPassword;
import com.wx.wiki.req.UserSaveReq;
import com.wx.wiki.resp.CommonResp;
import com.wx.wiki.resp.UserLoginResp;
import com.wx.wiki.resp.UserQueryResp;
import com.wx.wiki.resp.PageResp;
import com.wx.wiki.service.UserService;
import com.wx.wiki.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user") // 提取请求地址前缀
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private SnowFlake snowFlake;

    @GetMapping("/list")
    public CommonResp<PageResp<UserQueryResp>> list(@Valid UserQueryReq req) {
        CommonResp<PageResp<UserQueryResp>> resp = new CommonResp<>();
        PageResp<UserQueryResp> list = userService.list(req);
        resp.setContent(list);
        return resp;
    }

    // @RequestBody 就是接收 json 方式的提交，加这个才会接收到
    // form表单方式的提交，就不需要加
    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody UserSaveReq req) {
        // 十六进制的md5加密
        req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
        CommonResp resp = new CommonResp<>();
        userService.save(req);
        return resp;
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp delete(@PathVariable Long id) {
        CommonResp resp = new CommonResp<>();
        userService.delete(id);
        return resp;
    }

    @PostMapping("/reset-password")
    public CommonResp resetPassword(@Valid @RequestBody UserResetPassword req) {
        // 十六进制的md5加密
        req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
        CommonResp resp = new CommonResp<>();
        userService.resetPassword(req);
        return resp;
    }

    @PostMapping("/login")
    public CommonResp login(@Valid @RequestBody UserLoginReq req) {
        // 十六进制的md5加密
        req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
        CommonResp<UserLoginResp> resp = new CommonResp<>();
        UserLoginResp userLoginResp = userService.login(req);

        // 生成单点登录token并放入redis中
        Long token = snowFlake.nextId();
        LOG.info("生成单点登录token: {}, 并放入redis中", token);
        userLoginResp.setToken(token.toString());
        // userLoginResp 要放入 redis 需要序列化一下
        redisTemplate.opsForValue().set(token, JSONObject.toJSONString(userLoginResp), 3600 * 24, TimeUnit.SECONDS);
        resp.setContent(userLoginResp);
        return resp;
    }

    @GetMapping("/logout/{token}")
    public CommonResp logout(@PathVariable String token) {
        CommonResp resp = new CommonResp<>();
        // 清除redis的token
        redisTemplate.delete(token);
        LOG.info("从redis中删除token: {}", token);
        return resp;
    }
}
