package com.github.cuteluobo.livedanmuarchive.pojo.danmusender;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
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
    private List<BiliProcessedPartVideoData> partVideoDataList;
    /**
     * 分P索引 - 当前分P下的数据库分页表
     */
    private Map<AtomicInteger, AtomicInteger> partAndPageNumMap;

    public BiliProcessedVideoData() {
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
        this.partVideoDataList = partVideoDataList;
        partAndPageNumMap = partVideoDataList.stream()
                .map(d -> new AbstractMap.SimpleEntry<>(new AtomicInteger(), new AtomicInteger()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        ;
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
