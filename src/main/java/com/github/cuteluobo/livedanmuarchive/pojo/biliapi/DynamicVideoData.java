package com.github.cuteluobo.livedanmuarchive.pojo.biliapi;

import java.util.List;
import java.util.Map;

/**
 * 动态视频数据
 *
 * @author CuteLuoBo
 * @date 2023/3/16 11:20
 */
public class DynamicVideoData {
    /**
     * 视频的BV列表和时间戳
     */
    private List<Map.Entry<String,Long>> videoList;
    /**
     * 动态偏移ID
     */
    private long offsetId;

    public DynamicVideoData(long offsetId) {
        this.offsetId = offsetId;
    }

    public DynamicVideoData(List<Map.Entry<String,Long>> videoBvIdList, long offsetId) {
        this.videoList = videoBvIdList;
        this.offsetId = offsetId;
    }

    public List<Map.Entry<String, Long>> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<Map.Entry<String, Long>> videoList) {
        this.videoList = videoList;
    }

    public long getOffsetId() {
        return offsetId;
    }

    public void setOffsetId(long offsetId) {
        this.offsetId = offsetId;
    }
}
