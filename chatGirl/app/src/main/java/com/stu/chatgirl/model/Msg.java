package com.stu.chatgirl.model;


public class Msg {

    private String msg;
    private int type;

    public Msg(String msg, int type) {
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
