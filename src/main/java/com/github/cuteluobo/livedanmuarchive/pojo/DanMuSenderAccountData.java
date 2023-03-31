package com.github.cuteluobo.livedanmuarchive.pojo;

/**
 * 弹幕发送账户数据
 *
 * @author CuteLuoBo
 * @date 2023/3/8 15:12
 */
public class DanMuSenderAccountData {
    private String uid;
    private String nickName;
    private String userName;
    private String password;
    private String cookies;
    private boolean alive = true;

    public DanMuSenderAccountData() {
    }

    public DanMuSenderAccountData(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public DanMuSenderAccountData(String cookies) {
        this.cookies = cookies;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCookies() {
        return cookies;
    }

    public void setCookies(String cookies) {
        this.cookies = cookies;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
