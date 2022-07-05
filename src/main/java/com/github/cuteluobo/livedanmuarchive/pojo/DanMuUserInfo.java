package com.github.cuteluobo.livedanmuarchive.pojo;

/**
 * @author CuteLuoBo
 * @date 2022/2/12 20:42
 */
public class DanMuUserInfo {
    private String uid;
    private String nickName;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DanMuUserInfo{");
        sb.append("uid='").append(uid).append('\'');
        sb.append(", nickName='").append(nickName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
