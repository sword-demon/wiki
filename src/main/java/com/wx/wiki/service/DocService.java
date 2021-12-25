package com.wx.wiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wx.wiki.domain.Doc;
import com.wx.wiki.domain.DocExample;
import com.wx.wiki.mapper.DocMapper;
import com.wx.wiki.req.DocQueryReq;
import com.wx.wiki.req.DocSaveReq;
import com.wx.wiki.resp.DocQueryResp;
import com.wx.wiki.resp.PageResp;
import com.wx.wiki.util.CopyUtil;
import com.wx.wiki.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DocService {

    private static final Logger LOG = LoggerFactory.getLogger(DocService.class);

    @Resource
    private DocMapper docMapper;

    @Resource
    private SnowFlake snowFlake;

    public List<DocQueryResp> all() {

        DocExample docExample = new DocExample();
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
    public void save(DocSaveReq req) {
        Doc doc = CopyUtil.copy(req, Doc.class);
        if (ObjectUtils.isEmpty(req.getId())) {
            // 新增
            doc.setId(snowFlake.nextId());
            docMapper.insert(doc);
        } else {
            // 更新
            docMapper.updateByPrimaryKey(doc);
        }
    }

    /**
     * 删除电子书
     * @param id Long
     */
    public void delete(Long id) {
        docMapper.deleteByPrimaryKey(id);
    }
}
