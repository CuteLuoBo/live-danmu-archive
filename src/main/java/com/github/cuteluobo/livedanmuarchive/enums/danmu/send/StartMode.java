package com.github.cuteluobo.livedanmuarchive.enums.danmu.send;

/**
 * 弹幕发送模式
 *
 * @author CuteLuoBo
 * @date 2022/7/4 18:20
 */
public enum StartMode {
    /***/
    AUTO(1,"自动模式"),MANUAL(0,"手动模式");

    StartMode(int value, String comment) {
        this.value = value;
        this.comment = comment;
    }

    private final int value;
    private final String comment;


    public int getValue() {
        return value;
    }

    public String getComment() {
        return comment;
    }

}
