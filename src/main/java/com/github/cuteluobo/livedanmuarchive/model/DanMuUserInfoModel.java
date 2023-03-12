package com.github.cuteluobo.livedanmuarchive.model;

import com.github.cuteluobo.livedanmuarchive.pojo.DanMuUserInfo;

/**
 * 弹幕用户信息-数据库储存用Model
 * @author CuteLuoBo
 * @date 2022/4/4 14:57
 */
public class DanMuUserInfoModel extends DanMuUserInfo {
    /**
     * 储存ID
     */
    private Integer id;
    /**
     * 添加时间
     */
    private Long addTime;

    public DanMuUserInfoModel() {
    }

    public DanMuUserInfoModel(Integer id, Long addTime) {
        this.id = id;
        this.addTime = addTime;
    }

    public DanMuUserInfoModel(String nickName, Integer id, Long addTime) {
        super(nickName);
        this.id = id;
        this.addTime = addTime;
    }

    public DanMuUserInfoModel(String uid, String nickName, Integer id, Long addTime) {
        super(uid, nickName);
        this.id = id;
        this.addTime = addTime;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DanMuUserInfoModel{");
        sb.append("id=").append(id);
        sb.append(", addTime=").append(addTime);
        sb.append('}');
        return sb.toString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getAddTime() {
        return addTime;
    }

    public void setAddTime(Long addTime) {
        this.addTime = addTime;
    }
}
