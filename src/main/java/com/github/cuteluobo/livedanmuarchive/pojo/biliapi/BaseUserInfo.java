package com.github.cuteluobo.livedanmuarchive.pojo.biliapi;

/**
 * 账号用户数据
 * @author CuteLuoBo
 * @date 2023/1/9 22:13
 */
public class BaseUserInfo {
    private long uid;
    private boolean login = false;
    private int level;
    private String nickName;
    private String imgKey;
    private String subKey;
    private String cookie;
    private String accessKey;


    public BaseUserInfo() {
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getImgKey() {
        return imgKey;
    }

    public void setImgKey(String imgKey) {
        this.imgKey = imgKey;
    }

    public String getSubKey() {
        return subKey;
    }

    public void setSubKey(String subKey) {
        this.subKey = subKey;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public BaseUserInfo(int level, String nickName) {
        this.level = level;
        this.nickName = nickName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BaseUserInfo{");
        sb.append("uid=").append(uid);
        sb.append(", login=").append(login);
        sb.append(", level=").append(level);
        sb.append(", nickName='").append(nickName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
