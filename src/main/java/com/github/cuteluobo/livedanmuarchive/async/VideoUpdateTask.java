package com.github.cuteluobo.livedanmuarchive.async;

import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendTaskField;

/**
 * 视频监听任务
 * @author CuteLuoBo
 * @date 2023/3/30 15:31
 */
public abstract class VideoUpdateTask {

    private String uid;

    private String tagMatch = null;
    private String titleMatch= null;
    private String videoPartTimeRegular = ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getNormalValue();
    private String videoPartTimeFormat = ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getNormalValue();

    public VideoUpdateTask(String uid) {
        this.uid = uid;
    }

    public VideoUpdateTask(String uid, String tagMatch, String titleMatch) {
        this.uid = uid;
        this.tagMatch = tagMatch;
        this.titleMatch = titleMatch;
    }

    public VideoUpdateTask(String uid, String tagMatch, String titleMatch, String videoPartTimeRegular, String videoPartTimeFormat) {
        this.uid = uid;
        this.tagMatch = tagMatch;
        this.titleMatch = titleMatch;
        this.videoPartTimeRegular = videoPartTimeRegular;
        this.videoPartTimeFormat = videoPartTimeFormat;
    }

    /**
     * 更新视频任务
     * @return 创建的任务
     */
    public abstract Runnable updateLatestVideoId();

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTagMatch() {
        return tagMatch;
    }

    public void setTagMatch(String tagMatch) {
        this.tagMatch = tagMatch;
    }

    public String getTitleMatch() {
        return titleMatch;
    }

    public void setTitleMatch(String titleMatch) {
        this.titleMatch = titleMatch;
    }

    public String getVideoPartTimeRegular() {
        return videoPartTimeRegular;
    }

    public void setVideoPartTimeRegular(String videoPartTimeRegular) {
        this.videoPartTimeRegular = videoPartTimeRegular;
    }

    public String getVideoPartTimeFormat() {
        return videoPartTimeFormat;
    }

    public void setVideoPartTimeFormat(String videoPartTimeFormat) {
        this.videoPartTimeFormat = videoPartTimeFormat;
    }
}
