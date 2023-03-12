package com.github.cuteluobo.livedanmuarchive.pojo;

/**
 * B站弹幕自动发送账户数据
 * @author CuteLuoBo
 * @date 2023/3/8 15:18
 */
public class BiliDanMuSenderAccountData extends DanMuSenderAccountData {
    private String accessKey;

    public BiliDanMuSenderAccountData() {
    }

    public BiliDanMuSenderAccountData(String userName, String password) {
        super(userName, password);
    }




    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
}
