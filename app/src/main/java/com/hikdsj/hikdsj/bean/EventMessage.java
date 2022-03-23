package com.hikdsj.hikdsj.bean;

public class EventMessage {
    public static final int PAY_SUCCESS =513;
    public static final int ATTEN_SUCCESS =98513;
    private String message ="";
    private String content = "";
    private int code;

    private String openid = "";
    private String acc_token="";

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getAcc_token() {
        return acc_token;
    }

    public void setAcc_token(String acc_token) {
        this.acc_token = acc_token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public EventMessage() {
    }

    public EventMessage(String message, String openid, String acc_token) {
        this.message = message;
        this.openid = openid;
        this.acc_token = acc_token;
    }

    public EventMessage(String message) {
        this.message = message;
    }

    public EventMessage(int code) {
        this.code = code;
    }

    public EventMessage(String message, int code) {
        this.message = message;
        this.code = code;
    }
}
