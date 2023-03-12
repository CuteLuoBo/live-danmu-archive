package com.github.cuteluobo.livedanmuarchive.pojo.danmusender;

/**
 * 分段视频数据
 * @author CuteLuoBo
 * @date 2023/3/12 8:59
 */
public class BiliProcessedPartVideoData {
    private long cid;
    private long duration;
    private String partName;
    private long videoStartMillTime;
    private long videoEndMillTime;

    public BiliProcessedPartVideoData() {
    }

    public BiliProcessedPartVideoData(long cid, long duration, String partName, long videoStartMillTime, long videoEndMillTime) {
        this.cid = cid;
        this.duration = duration;
        this.partName = partName;
        this.videoStartMillTime = videoStartMillTime;
        this.videoEndMillTime = videoEndMillTime;
    }

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public long getVideoStartMillTime() {
        return videoStartMillTime;
    }

    public void setVideoStartMillTime(long videoStartMillTime) {
        this.videoStartMillTime = videoStartMillTime;
    }

    public long getVideoEndMillTime() {
        return videoEndMillTime;
    }

    public void setVideoEndMillTime(long videoEndMillTime) {
        this.videoEndMillTime = videoEndMillTime;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ProcessedPartVideoData{");
        sb.append("cid=").append(cid);
        sb.append(", duration=").append(duration);
        sb.append(", partName='").append(partName).append('\'');
        sb.append(", videoStartMillTime=").append(videoStartMillTime);
        sb.append(", videoEndMillTime=").append(videoEndMillTime);
        sb.append('}');
        return sb.toString();
    }
}
