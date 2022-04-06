package com.github.cuteluobo.livedanmuarchive.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author CuteLuoBo
 * @date 2022/2/12 20:33
 */
@JsonIgnoreProperties({ "lUid", "lImid","iGender","sAvatarUrl","iNobleLevel" })
public class HuYaUserInfo extends DanMuUserInfo {
    private int lUid = 0;
    private int lImid = 0;
    private int iGender = 0;
    private String sAvatarUrl = "";

    private int iNobleLevel = 0;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("HuYaUserInfo{");
        sb.append("nickName=").append(getNickName());
//        sb.append(",lUid=").append(lUid);
//        sb.append(", lImid=").append(lImid);
//        sb.append(", iGender=").append(iGender);
//        sb.append(", sAvatarUrl='").append(sAvatarUrl).append('\'');
//        sb.append(", iNobleLevel=").append(iNobleLevel);
        sb.append('}');
        return sb.toString();
    }

    public int getlUid() {
        return lUid;
    }

    public void setlUid(int lUid) {
        this.lUid = lUid;
    }

    public int getlImid() {
        return lImid;
    }

    public void setlImid(int lImid) {
        this.lImid = lImid;
    }

    public int getiGender() {
        return iGender;
    }

    public void setiGender(int iGender) {
        this.iGender = iGender;
    }

    public String getsAvatarUrl() {
        return sAvatarUrl;
    }

    public void setsAvatarUrl(String sAvatarUrl) {
        this.sAvatarUrl = sAvatarUrl;
    }

    public int getiNobleLevel() {
        return iNobleLevel;
    }

    public void setiNobleLevel(int iNobleLevel) {
        this.iNobleLevel = iNobleLevel;
    }
}
