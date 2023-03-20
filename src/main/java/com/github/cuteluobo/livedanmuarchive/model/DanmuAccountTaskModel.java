package com.github.cuteluobo.livedanmuarchive.model;

public class DanmuAccountTaskModel {
    private Integer id;

    private Integer danmuSenderTaskId;

    private String senderUid;

    private String videoId;

    private Integer lastVideoPartIndex;

    private Long lastVideoPartCid;

    private Integer pageIndex = 0;

    private Integer pageSize = 0;

    private Integer lastDanmuIndex;

    private Boolean stop = false;

    private Long createTime;

    private Long updateTime;

    private Long finishTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDanmuSenderTaskId() {
        return danmuSenderTaskId;
    }

    public void setDanmuSenderTaskId(Integer danmuSenderTaskId) {
        this.danmuSenderTaskId = danmuSenderTaskId;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public Integer getLastVideoPartIndex() {
        return lastVideoPartIndex;
    }

    public void setLastVideoPartIndex(Integer lastVideoPartIndex) {
        this.lastVideoPartIndex = lastVideoPartIndex;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getLastDanmuIndex() {
        return lastDanmuIndex;
    }

    public void setLastDanmuIndex(Integer lastDanmuIndex) {
        this.lastDanmuIndex = lastDanmuIndex;
    }

    public Boolean getStop() {
        return stop;
    }

    public void setStop(Boolean stop) {
        this.stop = stop;
    }

    public Long getLastVideoPartCid() {
        return lastVideoPartCid;
    }

    public void setLastVideoPartCid(Long lastVideoPartCid) {
        this.lastVideoPartCid = lastVideoPartCid;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", danmuSenderTaskId=").append(danmuSenderTaskId);
        sb.append(", senderUid=").append(senderUid);
        sb.append(", videoId=").append(videoId);
        sb.append(", lastVideoPartIndex=").append(lastVideoPartIndex);
        sb.append(", lastVideoPartCid=").append(lastVideoPartCid);
        sb.append(", pageIndex=").append(pageIndex);
        sb.append(", pageSize=").append(pageSize);
        sb.append(", lastDanmuId=").append(lastDanmuIndex);
        sb.append(", stop=").append(stop);
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
        DanmuAccountTaskModel other = (DanmuAccountTaskModel) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getDanmuSenderTaskId() == null ? other.getDanmuSenderTaskId() == null : this.getDanmuSenderTaskId().equals(other.getDanmuSenderTaskId()))
            && (this.getSenderUid() == null ? other.getSenderUid() == null : this.getSenderUid().equals(other.getSenderUid()))
            && (this.getVideoId() == null ? other.getVideoId() == null : this.getVideoId().equals(other.getVideoId()))
            && (this.getLastVideoPartIndex() == null ? other.getLastVideoPartIndex() == null : this.getLastVideoPartIndex().equals(other.getLastVideoPartIndex()))
            && (this.getLastVideoPartCid() == null ? other.getLastVideoPartCid() == null : this.getLastVideoPartCid().equals(other.getLastVideoPartCid()))
            && (this.getPageIndex() == null ? other.getPageIndex() == null : this.getPageIndex().equals(other.getPageIndex()))
            && (this.getPageSize() == null ? other.getPageSize() == null : this.getPageSize().equals(other.getPageSize()))
            && (this.getLastDanmuIndex() == null ? other.getLastDanmuIndex() == null : this.getLastDanmuIndex().equals(other.getLastDanmuIndex()))
            && (this.getStop() == null ? other.getStop() == null : this.getStop().equals(other.getStop()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getFinishTime() == null ? other.getFinishTime() == null : this.getFinishTime().equals(other.getFinishTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getDanmuSenderTaskId() == null) ? 0 : getDanmuSenderTaskId().hashCode());
        result = prime * result + ((getSenderUid() == null) ? 0 : getSenderUid().hashCode());
        result = prime * result + ((getVideoId() == null) ? 0 : getVideoId().hashCode());
        result = prime * result + ((getLastVideoPartIndex() == null) ? 0 : getLastVideoPartIndex().hashCode());
        result = prime * result + ((getLastVideoPartCid() == null) ? 0 : getLastVideoPartCid().hashCode());
        result = prime * result + ((getPageIndex() == null) ? 0 : getPageIndex().hashCode());
        result = prime * result + ((getPageSize() == null) ? 0 : getPageSize().hashCode());
        result = prime * result + ((getLastDanmuIndex() == null) ? 0 : getLastDanmuIndex().hashCode());
        result = prime * result + ((getStop() == null) ? 0 : getStop().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getFinishTime() == null) ? 0 : getFinishTime().hashCode());
        return result;
    }
}