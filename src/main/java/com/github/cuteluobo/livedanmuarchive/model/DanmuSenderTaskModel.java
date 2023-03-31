package com.github.cuteluobo.livedanmuarchive.model;

public class DanmuSenderTaskModel {
    private Integer id;

    private String platform;

    private String videoCreatorUid;

    private String videoId;

    private Long videoCreatedTime;

    private Long danmuTotalNum = 0L;

    private Long danmuSentNum = 0L;

    private Integer pageSize = 0;

    private Boolean skip = false;

    private Boolean taskFail = false;

    private Long createTime;

    private Long updateTime;

    private Long finishTime;

    public DanmuSenderTaskModel() {
    }

    public DanmuSenderTaskModel(String platform, String videoCreatorUid, String videoId, Long videoCreatedTime) {
        this.platform = platform;
        this.videoCreatorUid = videoCreatorUid;
        this.videoId = videoId;
        this.videoCreatedTime = videoCreatedTime;
    }

    public void setTime() {
        long time = System.currentTimeMillis();
        createTime = updateTime = time;
    }

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

    public Long getDanmuTotalNum() {
        return danmuTotalNum;
    }

    public void setDanmuTotalNum(Long danmuTotalNum) {
        this.danmuTotalNum = danmuTotalNum;
    }

    public Long getDanmuSentNum() {
        return danmuSentNum;
    }

    public void setDanmuSentNum(Long danmuSentNum) {
        this.danmuSentNum = danmuSentNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
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

    public void setVideoCreatedTime(Long videoCreatedTime) {
        this.videoCreatedTime = videoCreatedTime;
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

    public Long getVideoCreatedTime() {
        return videoCreatedTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", platform=").append(platform);
        sb.append(", videoCreatorUid=").append(videoCreatorUid);
        sb.append(", videoId=").append(videoId);
        sb.append(", videoCreatedTime=").append(videoCreatedTime);
        sb.append(", danmuTotalNum=").append(danmuTotalNum);
        sb.append(", danmuSentNum=").append(danmuSentNum);
        sb.append(", pageSize=").append(pageSize);
        sb.append(", skip=").append(skip);
        sb.append(", fail=").append(taskFail);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", finishTime=").append(finishTime);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        DanmuSenderTaskModel other = (DanmuSenderTaskModel) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPlatform() == null ? other.getPlatform() == null : this.getPlatform().equals(other.getPlatform()))
            && (this.getVideoCreatorUid() == null ? other.getVideoCreatorUid() == null : this.getVideoCreatorUid().equals(other.getVideoCreatorUid()))
            && (this.getVideoId() == null ? other.getVideoId() == null : this.getVideoId().equals(other.getVideoId()))
            && (this.getVideoCreatedTime() == null ? other.getVideoCreatedTime() == null : this.getVideoCreatedTime().equals(other.getVideoCreatedTime()))
            && (this.getDanmuTotalNum() == null ? other.getDanmuTotalNum() == null : this.getDanmuTotalNum().equals(other.getDanmuTotalNum()))
            && (this.getDanmuSentNum() == null ? other.getDanmuSentNum() == null : this.getDanmuSentNum().equals(other.getDanmuSentNum()))
            && (this.getPageSize() == null ? other.getPageSize() == null : this.getPageSize().equals(other.getPageSize()))
            && (this.getSkip() == null ? other.getSkip() == null : this.getSkip().equals(other.getSkip()))
            && (this.getTaskFail() == null ? other.getTaskFail() == null : this.getTaskFail().equals(other.getTaskFail()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getFinishTime() == null ? other.getFinishTime() == null : this.getFinishTime().equals(other.getFinishTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPlatform() == null) ? 0 : getPlatform().hashCode());
        result = prime * result + ((getVideoCreatorUid() == null) ? 0 : getVideoCreatorUid().hashCode());
        result = prime * result + ((getVideoId() == null) ? 0 : getVideoId().hashCode());
        result = prime * result + ((getVideoCreatedTime() == null) ? 0 : getVideoCreatedTime().hashCode());
        result = prime * result + ((getDanmuTotalNum() == null) ? 0 : getDanmuTotalNum().hashCode());
        result = prime * result + ((getDanmuSentNum() == null) ? 0 : getDanmuSentNum().hashCode());
        result = prime * result + ((getPageSize() == null) ? 0 : getPageSize().hashCode());
        result = prime * result + ((getSkip() == null) ? 0 : getSkip().hashCode());
        result = prime * result + ((getTaskFail() == null) ? 0 : getTaskFail().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getFinishTime() == null) ? 0 : getFinishTime().hashCode());
        return result;
    }
}