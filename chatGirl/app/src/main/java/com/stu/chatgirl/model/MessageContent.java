package com.stu.chatgirl.model;


public class MessageContent {

    private String msg;
    private int type;

    public MessageContent(String msg, int type) {
        this.msg = msg;
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public int getType() {
        return type;
    }
}
