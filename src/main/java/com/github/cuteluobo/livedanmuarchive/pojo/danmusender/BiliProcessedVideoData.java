package com.github.cuteluobo.livedanmuarchive.pojo.danmusender;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 处理过后的视频数据
 * @author CuteLuoBo
 * @date 2023/3/12 8:56
 */
public class BiliProcessedVideoData extends ProcessVideoData {
    private long avId;
    private String bvId;
    private String videoName;
    private String creatorUid;
    private long createTime;
    private List<BiliProcessedPartVideoData> partVideoDataList;
    /**
     * 分P对应的分页索引表
     */
    private List<AtomicInteger> pageIndexList;

    public BiliProcessedVideoData() {
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public BiliProcessedVideoData(long avId, String videoName) {
        this.avId = avId;
        this.videoName = videoName;
    }

    public BiliProcessedVideoData(String bvId, String videoName) {
        this.bvId = bvId;
        this.videoName = videoName;
    }

    public long getAvId() {
        return avId;
    }

    public void setAvId(long avId) {
        this.avId = avId;
    }

    public String getBvId() {
        return bvId;
    }

    public void setBvId(String bvId) {
        this.bvId = bvId;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public List<BiliProcessedPartVideoData> getPartVideoDataList() {
        return partVideoDataList;
    }

    /**
     * 设置视频数据同时更新缓存的分P与弹幕分页数据列表
     * @param partVideoDataList
     */
    public void setPartVideoDataList(List<BiliProcessedPartVideoData> partVideoDataList) {
        if (partVideoDataList == null) {
            partVideoDataList = new ArrayList<>(0);
        }
        this.partVideoDataList = partVideoDataList;
        pageIndexList = new ArrayList<>(partVideoDataList.size());
        for (int i = 0; i < partVideoDataList.size(); i++) {
            pageIndexList.add(new AtomicInteger());
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ProcessedVideoData{");
        sb.append("avId=").append(avId);
        sb.append(", bvId='").append(bvId).append('\'');
        sb.append(", videoName='").append(videoName).append('\'');
        sb.append(", partVideoDataList=").append(partVideoDataList);
        sb.append('}');
        return sb.toString();
    }

    public List<AtomicInteger> getPageIndexList() {
        return pageIndexList;
    }
}
