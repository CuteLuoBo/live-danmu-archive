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


    public BaseUserInfo() {
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
}
