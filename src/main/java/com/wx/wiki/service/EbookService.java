package com.wx.wiki.service;

import com.wx.wiki.domain.Ebook;
import com.wx.wiki.domain.EbookExample;
import com.wx.wiki.mapper.EbookMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EbookService {

    @Resource
    private EbookMapper ebookMapper;

    public List<Ebook> list() {
        return ebookMapper.selectByExample(new EbookExample());
    }
}
