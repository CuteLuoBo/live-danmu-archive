package com.github.cuteluobo.livedanmuarchive.pojo;

/**
 * B站弹幕自动发送账户数据
 * @author CuteLuoBo
 * @date 2023/3/8 15:18
 */
public class BiliDanMuSenderAccountData extends DanMuSenderAccountData {
    private String accessKey;
    private String appKey;
    private String appSec;
    private int level = 0;

    public BiliDanMuSenderAccountData() {
    }

    public BiliDanMuSenderAccountData(String userName, String password) {
        super(userName, password);
    }

    public BiliDanMuSenderAccountData(String accessKey, String appKey, String appSec) {
        this.accessKey = accessKey;
        this.appKey = appKey;
        this.appSec = appSec;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSec() {
        return appSec;
    }

    public void setAppSec(String appSec) {
        this.appSec = appSec;
    }

    public BiliDanMuSenderAccountData(String cookies) {
        super(cookies);
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
