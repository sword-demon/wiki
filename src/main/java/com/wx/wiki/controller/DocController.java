package com.wx.wiki.controller;

import com.wx.wiki.req.DocQueryReq;
import com.wx.wiki.req.DocSaveReq;
import com.wx.wiki.resp.DocQueryResp;
import com.wx.wiki.resp.CommonResp;
import com.wx.wiki.resp.PageResp;
import com.wx.wiki.service.DocService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/doc") // 提取请求地址前缀
public class DocController {

    @Resource
    private DocService docService;

    @GetMapping("/all")
    public CommonResp<List<DocQueryResp>> all() {
        CommonResp<List<DocQueryResp>> resp = new CommonResp<>();
        List<DocQueryResp> list = docService.all();
        resp.setContent(list);
        return resp;
    }

    @GetMapping("/list")
    public CommonResp<PageResp<DocQueryResp>> list(@Valid DocQueryReq req) {
        CommonResp<PageResp<DocQueryResp>> resp = new CommonResp<>();
        PageResp<DocQueryResp> list = docService.list(req);
        resp.setContent(list);
        return resp;
    }

    // @RequestBody 就是接收 json 方式的提交，加这个才会接收到
    // form表单方式的提交，就不需要加
    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody DocSaveReq req) {
        System.out.println(req.getParent());
        CommonResp resp = new CommonResp<>();
        docService.save(req);
        return resp;
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp delete(@PathVariable Long id) {
        CommonResp resp = new CommonResp<>();
        docService.delete(id);
        return resp;
    }
}
