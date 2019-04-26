package com.me.hiet.utils.dataInfo;

public class MusicCaptionInfo {

    private String captionText;    //  字幕文字
    private long captionStartTime; // 字幕开始时间 （单位微秒）
    private long captionDurtion;   // 字幕显示时长（单位微秒）

    public String getCaptionText() {
        return captionText;
    }

    public void setCaptionText(String captionText) {
        this.captionText = captionText;
    }

    public long getCaptionStartTime() {
        return captionStartTime;
    }

    public void setCaptionStartTime(long captionStartTime) {
        this.captionStartTime = captionStartTime;
    }

    public long getCaptionDurtion() {
        return captionDurtion;
    }

    public void setCaptionDurtion(long captionDurtion) {
        this.captionDurtion = captionDurtion;
    }
}
