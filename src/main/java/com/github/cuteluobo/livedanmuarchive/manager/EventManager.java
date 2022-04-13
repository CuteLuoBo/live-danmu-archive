package com.github.cuteluobo.livedanmuarchive.manager;

import com.github.cuteluobo.livedanmuarchive.listener.EventListener;
import com.github.cuteluobo.livedanmuarchive.listener.result.EventResult;


/**
 * 事件管理器接口
 *
 * @author CuteLuoBo
 * @param <U> 事件类型枚举类
 * @param <T> 事件数据
 */
public interface EventManager<U extends Enum<U>,T extends EventResult> {
    /**
     * 订阅事件
     *
     * @param eventType     事件类型
     * @param eventListener 事件监听器
     */
    void subscribe(U eventType, EventListener<T> eventListener);

    /**
     * 取消订阅
     * @param eventType     事件类型
     * @param eventListener 事件监听器
     */
    void unsubscribe(U eventType, EventListener<T> eventListener);

    /**
     * 事件通知
     * @param eventType 事件类型
     * @param result 事件数据
     */
    void notify(U eventType, T result);
}
