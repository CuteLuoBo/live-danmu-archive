package com.github.cuteluobo.livedanmuarchive.model;

/**
 * 弹幕任务计划储存对象
 *
 * @author: CuteLuoBo
 * @date: 2025/7/8  11:04
 * @version: 1.0.0
 */
public class DanMuTaskPlanModel {
    private Integer id ;
    private String platform;

    private String videoCreatorUid;

    private String videoId;

    private Long videoCreatedTime;

    /**
     * 分割时间(ms)
     */
    private Long slicedTime;

    private Integer pageCurrent;

    private Boolean skip = false;

    private Boolean taskFail = false;

    private Long createTime;

    private Long updateTime;

    private Long finishTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getVideoCreatorUid() {
        return videoCreatorUid;
    }

    public void setVideoCreatorUid(String videoCreatorUid) {
        this.videoCreatorUid = videoCreatorUid;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public Long getVideoCreatedTime() {
        return videoCreatedTime;
    }

    public void setVideoCreatedTime(Long videoCreatedTime) {
        this.videoCreatedTime = videoCreatedTime;
    }

    public Long getSlicedTime() {
        return slicedTime;
    }

    public void setSlicedTime(Long slicedTime) {
        this.slicedTime = slicedTime;
    }

    public Integer getPageCurrent() {
        return pageCurrent;
    }

    public void setPageCurrent(Integer pageCurrent) {
        this.pageCurrent = pageCurrent;
    }

    public Boolean getSkip() {
        return skip;
    }

    public void setSkip(Boolean skip) {
        this.skip = skip;
    }

    public Boolean getTaskFail() {
        return taskFail;
    }

    public void setTaskFail(Boolean taskFail) {
        this.taskFail = taskFail;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Long finishTime) {
        this.finishTime = finishTime;
    }
}

