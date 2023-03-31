package com.github.cuteluobo.livedanmuarchive.service;

import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.ProcessVideoData;

/**
 * 弹幕自动发送服务
 *
 * @author CuteLuoBo
 * @date 2023/1/9 17:25
 */
public interface DanMuAutoSendService<T extends ProcessVideoData> {
    /**
     * 启动发送任务
     * @param videoId 视频ID
     * @param fileSaveName 弹幕保存名称
     */
    void startSendTask(T videoId,String fileSaveName) throws ServiceException;

    /**
     * 中断发送任务
     * @return 当前任务ID
     */
    int stopSendTask();

    /**
     * 恢复发送任务
     * @param taskId 任务ID
     */
    void renewTask(int taskId);
}
