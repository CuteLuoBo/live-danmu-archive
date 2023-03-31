package com.github.cuteluobo.livedanmuarchive.pojo.biliapi;

import java.util.List;

/**
 * 视频详细信息
 *https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/video/info.md#%E8%8E%B7%E5%8F%96%E8%A7%86%E9%A2%91%E8%AF%A6%E7%BB%86%E4%BF%A1%E6%81%AF(web%E7%AB%AF)
 * @author CuteLuoBo
 * @date 2022/12/28 13:37
 */
public class VideoAllInfo {
    private String bvId;
    private int avId;
    /**
     * 视频总数
     */
    private int videos;
    /**
     * 分区TID
     */
    private int tid;
    /**
     * 分区名称
     */
    private String tName;
    /**
     * 版权：转载2/原创1
     */
    private int copyright;
    /**
     * 稿件封面图片url
     */
    private String pic;
    /**
     * 标题
     */
    private String title;
    /**
     * 稿件发布时间（秒时间戳）
     */
    private int pubDate;
    /**
     * 视频投稿时间(秒时间戳)
     */
    private int ctime;
    /**
     * 简介
     */
    private String desc;
    /**
     * V2版简介
     */
    private List<String> descV2;
    /**
     * 视频状态
     */
    private int state;
    /**
     * (所有分P)稿件总时长(s)
     */
    private long duration;

    /**
     * 创建者UID
     */
    private String creatorUid;
    /**
     * 稿件分P信息
     */
    private List<VideoPage> pages;
    /**
     * 标签列表
     */
    private List<String> tagList;

    public String getCreatorUid() {
        return creatorUid;
    }

    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }

    public String getBvId() {
        return bvId;
    }

    public void setBvId(String bvId) {
        this.bvId = bvId;
    }

    public int getAvId() {
        return avId;
    }

    public void setAvId(int avId) {
        this.avId = avId;
    }

    public int getVideos() {
        return videos;
    }

    public void setVideos(int videos) {
        this.videos = videos;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public String gettName() {
        return tName;
    }

    public void settName(String tName) {
        this.tName = tName;
    }

    public int getCopyright() {
        return copyright;
    }

    public void setCopyright(int copyright) {
        this.copyright = copyright;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPubDate() {
        return pubDate;
    }

    public void setPubDate(int pubDate) {
        this.pubDate = pubDate;
    }

    public int getCtime() {
        return ctime;
    }

    public void setCtime(int ctime) {
        this.ctime = ctime;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getDescV2() {
        return descV2;
    }

    public void setDescV2(List<String> descV2) {
        this.descV2 = descV2;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<VideoPage> getPages() {
        return pages;
    }

    public void setPages(List<VideoPage> pages) {
        this.pages = pages;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }
}
