package com.github.cuteluobo.livedanmuarchive.pojo;

/**
 * B站弹幕自动发送账户数据
 * @author CuteLuoBo
 * @date 2023/3/8 15:18
 */
public class BiliDanMuSenderAccountData extends DanMuSenderAccountData {
    private String accessKey;
    private int level = 0;

    public BiliDanMuSenderAccountData() {
    }

    public BiliDanMuSenderAccountData(String userName, String password) {
        super(userName, password);
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
