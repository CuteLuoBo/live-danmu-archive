package com.github.cuteluobo.livedanmuarchive.pojo;

/**
 * @author CuteLuoBo
 * @date 2022/2/12 20:42
 */
public class DanMuUserInfo {
    private String nickName;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DanMuUserIfo{");
        sb.append("userName='").append(nickName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
