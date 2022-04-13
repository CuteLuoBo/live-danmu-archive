package com.github.cuteluobo.livedanmuarchive.listener;

import com.github.cuteluobo.livedanmuarchive.listener.result.EventResult;

/**
 * 事件监听器
 *
 * @author CuteLuoBo
 * @date 2022/4/7 18:43
 */
public interface EventListener<T extends EventResult> {
    /**
     * 接收事件时的操作
     * @param eventResult 事件结果
     */
    void doEvent(T eventResult);
}
