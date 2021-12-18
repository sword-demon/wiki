package com.wx.wiki.controller;

import com.wx.wiki.req.EbookReq;
import com.wx.wiki.resp.CommonResp;
import com.wx.wiki.resp.EbookResp;
import com.wx.wiki.resp.PageResp;
import com.wx.wiki.service.EbookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/ebook") // 提取请求地址前缀
public class EbookController {

    @Resource
    private EbookService ebookService;

    @GetMapping("/list")
    public CommonResp<PageResp<EbookResp>> list(EbookReq req) {
        CommonResp<PageResp<EbookResp>> resp = new CommonResp<>();
        PageResp<EbookResp> list = ebookService.list(req);
        resp.setContent(list);
        return resp;
    }
}
