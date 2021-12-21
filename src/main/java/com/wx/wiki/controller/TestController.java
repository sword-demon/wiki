package com.wx.wiki.controller;

import com.wx.wiki.domain.Test;
import com.wx.wiki.service.TestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 测试代码
 */
@RestController
public class TestController {

    @Resource
    private TestService testService;

    @Value("${test.hello:TEST}")
    private String testHello;

    @RequestMapping("/hello")
    public String hello() {
        return "hello world" + testHello;
    }

    @GetMapping("/test/list")
    public List<Test> list() {
        return testService.list();
    }
}
