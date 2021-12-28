package com.wx.wiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wx.wiki.domain.Content;
import com.wx.wiki.domain.Doc;
import com.wx.wiki.domain.DocExample;
import com.wx.wiki.exception.BusinessException;
import com.wx.wiki.exception.BusinessExceptionCode;
import com.wx.wiki.mapper.ContentMapper;
import com.wx.wiki.mapper.DocMapper;
import com.wx.wiki.mapper.DocMapperCust;
import com.wx.wiki.req.DocQueryReq;
import com.wx.wiki.req.DocSaveReq;
import com.wx.wiki.resp.DocQueryResp;
import com.wx.wiki.resp.PageResp;
import com.wx.wiki.util.CopyUtil;
import com.wx.wiki.util.RedisUtil;
import com.wx.wiki.util.RequestContext;
import com.wx.wiki.util.SnowFlake;
import com.wx.wiki.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DocService {

    private static final Logger LOG = LoggerFactory.getLogger(DocService.class);

    @Resource
    private DocMapper docMapper;

    @Resource
    private DocMapperCust docMapperCust;

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private SnowFlake snowFlake;

    @Resource
    private WsService wsService;

    public List<DocQueryResp> all(Long ebookId) {

        DocExample docExample = new DocExample();
        // 如果 ebookId = null 那就搜不到文档
        docExample.createCriteria().andEbookIdEqualTo(ebookId);
        docExample.setOrderByClause("sort asc"); // 排序
        // 只对第一个select有用 所以最好就把这2个放在一起
        List<Doc> docList = docMapper.selectByExample(docExample);
        // 列表复制 => 工具类的列表复制
        List<DocQueryResp> list = CopyUtil.copyList(docList, DocQueryResp.class);

        return list;
    }

    public PageResp<DocQueryResp> list(DocQueryReq req) {

        DocExample docExample = new DocExample();
        docExample.setOrderByClause("sort asc");
        DocExample.Criteria criteria = docExample.createCriteria();
        // 只对第一个select有用 所以最好就把这2个放在一起
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Doc> docList = docMapper.selectByExample(docExample);

        PageInfo<Doc> pageInfo = new PageInfo<>(docList);
        LOG.info("总行数: {}", pageInfo.getTotal());
        LOG.info("总页数: {}", pageInfo.getPages());

        // 列表复制 => 工具类的列表复制
        List<DocQueryResp> list = CopyUtil.copyList(docList, DocQueryResp.class);

        PageResp<DocQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);

        return pageResp;
    }

    /**
     * save 既要支持新增也要支持更新
     */
    @Transactional
    public void save(DocSaveReq req) {
        Doc doc = CopyUtil.copy(req, Doc.class);
        Content content = CopyUtil.copy(req, Content.class);
        if (ObjectUtils.isEmpty(req.getId())) {
            // 新增
            doc.setId(snowFlake.nextId());
            // 新增的时候设置阅读数和投票数为0 不然会为null
            doc.setViewCount(0);
            doc.setVoteCount(0);
            docMapper.insert(doc);

            // 保存文档内容
            content.setId(doc.getId());
            contentMapper.insert(content);
        } else {
            // 更新
            docMapper.updateByPrimaryKey(doc);
            // 全部字段和打字段的更新
            int count = contentMapper.updateByPrimaryKeyWithBLOBs(content);
            // 可能表里没有关联的文档id 就需要插入一条数据
            if (count == 0) {
                contentMapper.insert(content);
            }
        }
    }

    /**
     * 删除文档
     *
     * @param id Long
     */
    public void delete(Long id) {
        docMapper.deleteByPrimaryKey(id);
    }

    // 查找文档内容
    public String findContent(Long id) {
        Content content = contentMapper.selectByPrimaryKey(id);
        // 文档阅读数加1
        docMapperCust.increaseViewCount(id);
        if (!ObjectUtils.isEmpty(content)) {
            return content.getContent();
        } else {
            return "";
        }
    }

    /**
     * 批量删除文档
     *
     * @param ids
     */
    public void delete(List<String> ids) {
        DocExample docExample = new DocExample();
        DocExample.Criteria criteria = docExample.createCriteria();
        criteria.andIdIn(ids);
        docMapper.deleteByExample(docExample);
    }

    // 点赞
    public void vote(Long id) {
        // 文档点赞数加1
//        docMapperCust.increaseVoteCount(id);

        // 远程IP+doc.id作为key，24小时内不能重复点击
        String key = RequestContext.getRemoteAddr();
        if (redisUtil.validateRepeat("DOC_VOTE_" + id + "_" + key, 3600 * 24)) {
            docMapperCust.increaseVoteCount(id);
        } else {
            throw new BusinessException(BusinessExceptionCode.VOTE_REPEAT);
        }

        // 推送消息
        // 根据id查询文档名称
        Doc docDb = docMapper.selectByPrimaryKey(id);
        // 取出日志流水号
        String log_id = MDC.get("LOG_ID");
        wsService.sendInfo("【" + docDb.getName() + "】被点赞!", log_id);
    }



    // 定时任务更新电子书信息
    public void updateEbookInfo() {
        docMapperCust.updateEbookInfo();
    }
}
