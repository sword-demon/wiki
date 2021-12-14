package com.wx.wiki.service;

import com.wx.wiki.domain.Ebook;
import com.wx.wiki.domain.EbookExample;
import com.wx.wiki.mapper.EbookMapper;
import com.wx.wiki.req.EbookReq;
import com.wx.wiki.resp.EbookResp;
import com.wx.wiki.util.CopyUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EbookService {

    @Resource
    private EbookMapper ebookMapper;

    public List<EbookResp> list(EbookReq req) {
        EbookExample ebookExample = new EbookExample();
        EbookExample.Criteria criteria = ebookExample.createCriteria();
        // 参数可以为空
        if (!ObjectUtils.isEmpty(req.getName())) {
            criteria.andNameLike("%" + req.getName() + "%");
        }
        List<Ebook> ebookList = ebookMapper.selectByExample(ebookExample);

//        List<EbookResp> respList = new ArrayList<>();
//        for (Ebook ebook : ebookList) {
////            EbookResp ebookResp = new EbookResp();
//            // 一个一个写，就比较麻烦
////            ebookResp.setId(ebook.getId());
////            BeanUtils.copyProperties(ebook, ebookResp); // 从哪里拷贝到哪里 实现对象的复制
//
//            // 工具类的复制
//            EbookResp ebookResp = CopyUtil.copy(ebook, EbookResp.class);
//
//            respList.add(ebookResp);
//        }

        // 列表复制 => 工具类的列表复制
        List<EbookResp> list = CopyUtil.copyList(ebookList, EbookResp.class);

        return list;
    }
}
