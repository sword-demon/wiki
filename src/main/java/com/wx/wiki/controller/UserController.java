package com.wx.wiki.controller;

import com.wx.wiki.req.UserLoginReq;
import com.wx.wiki.req.UserQueryReq;
import com.wx.wiki.req.UserResetPassword;
import com.wx.wiki.req.UserSaveReq;
import com.wx.wiki.resp.CommonResp;
import com.wx.wiki.resp.UserLoginResp;
import com.wx.wiki.resp.UserQueryResp;
import com.wx.wiki.resp.PageResp;
import com.wx.wiki.service.UserService;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/user") // 提取请求地址前缀
public class UserController {

    @Resource
    private UserService userService;

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

    @PostMapping("/logn")
    public CommonResp logn(@Valid @RequestBody UserLoginReq req) {
        // 十六进制的md5加密
        req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
        CommonResp<UserLoginResp> resp = new CommonResp<>();
        UserLoginResp userLoginResp = userService.login(req);
        resp.setContent(userLoginResp);
        return resp;
    }
}
