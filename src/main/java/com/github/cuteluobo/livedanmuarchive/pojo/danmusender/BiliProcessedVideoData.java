package com.github.cuteluobo.livedanmuarchive.pojo.danmusender;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
     * 分P索引 - 当前分P下的数据库分页表
     */
    private Map<AtomicInteger, AtomicInteger> partAndPageNumMap;

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
        partAndPageNumMap = new LinkedHashMap<>(partVideoDataList.size());
        for (int i = 0; i < partVideoDataList.size(); i++) {
            partAndPageNumMap.put(new AtomicInteger(i), new AtomicInteger());
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

    public Map<AtomicInteger, AtomicInteger> getPartAndPageNumMap() {
        return partAndPageNumMap;
    }
}
