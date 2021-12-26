package com.wx.wiki.exception;

public enum BusinessExceptionCode {

    USER_LOGIN_NAME_EXIST("登录名已存在"),
    ;

    private String desc;

    BusinessExceptionCode(String desc) {
        this.desc = desc;
    }

    public void setDesc(String desc) {
        this.desc = this.desc;
    }

    public String getDesc() {
        return this.desc;
    }
}
