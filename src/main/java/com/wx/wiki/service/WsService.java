package com.wx.wiki.service;

import com.wx.wiki.domain.Doc;
import com.wx.wiki.websocket.WebSocketServer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WsService {

    @Resource
    private WebSocketServer webSocketServer;

    // 异步执行
    @Async
    public void sendInfo(String message) {
        // 推送消息
        webSocketServer.sendInfo(message);
    }
}
