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
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/doc") // 提取请求地址前缀
public class DocController {

    @Resource
    private DocService docService;

    @GetMapping("/all/{ebookId}")
    public CommonResp<List<DocQueryResp>> all(@PathVariable Long ebookId) {
        CommonResp<List<DocQueryResp>> resp = new CommonResp<>();
        List<DocQueryResp> list = docService.all(ebookId);
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

    // 根据doc的主键id查找文档内容
    @GetMapping("/find-content/{id}")
    public CommonResp<String> findContent(@PathVariable Long id) {
        CommonResp<String> resp = new CommonResp<>();
        String content = docService.findContent(id);
        resp.setContent(content);
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

    // 删除
    @DeleteMapping("/delete/{ids}")
    public CommonResp delete(@PathVariable String ids) {
        CommonResp resp = new CommonResp<>();
        List<String> stringList = Arrays.asList(ids.split(","));
        docService.delete(stringList);
        return resp;
    }

    // 点赞
    @GetMapping("/vote/{id}")
    public CommonResp vote(@PathVariable Long id) {
        CommonResp resp = new CommonResp<>();
        docService.vote(id);
        return resp;
    }
}
