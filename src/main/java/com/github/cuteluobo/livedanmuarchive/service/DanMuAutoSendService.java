package com.github.cuteluobo.livedanmuarchive.service;

/**
 * 弹幕自动发送服务
 *
 * @author CuteLuoBo
 * @date 2023/1/9 17:25
 */
public interface DanMuAutoSendService {
    /**
     * 开始发送弹幕
     * @param videoId 视频ID
     */
    void startSend(String videoId);
}
