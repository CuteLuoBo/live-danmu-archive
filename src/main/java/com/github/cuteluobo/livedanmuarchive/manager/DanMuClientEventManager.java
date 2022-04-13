package com.github.cuteluobo.livedanmuarchive.manager;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuClientEventType;
import com.github.cuteluobo.livedanmuarchive.listener.EventListener;
import com.github.cuteluobo.livedanmuarchive.listener.result.DanMuClientEventResult;

import java.util.*;

/**
 * 弹幕客户端事件管理器
 *
 * @author CuteLuoBo
 * @date 2022/4/7 18:37
 */
public class DanMuClientEventManager implements EventManager<DanMuClientEventType,DanMuClientEventResult> {

    /**
     * 监听器Map
     */
    private Map<Enum<DanMuClientEventType>, List<EventListener<DanMuClientEventResult>>> listenerMap;


    /**
     * 初始化map
     */
    public DanMuClientEventManager() {
        listenerMap = new HashMap<>(DanMuClientEventType.values().length);
        for (Enum<DanMuClientEventType> e :
                DanMuClientEventType.values()) {
            listenerMap.put(e, new ArrayList<>(10));
        }
    }

    /**
     * 订阅事件
     *
     * @param eventType     事件类型
     * @param eventListener 事件监听器
     */
    @Override
    public void subscribe(DanMuClientEventType eventType, EventListener<DanMuClientEventResult> eventListener) {
        List<EventListener<DanMuClientEventResult>> eventListenerList = listenerMap.get(eventType);
        eventListenerList.add(eventListener);
    }

    /**
     * 取消订阅
     *
     * @param eventType     事件类型
     * @param eventListener 事件监听器
     */
    @Override
    public void unsubscribe(DanMuClientEventType eventType, EventListener<DanMuClientEventResult> eventListener) {
        List<EventListener<DanMuClientEventResult>> eventListenerList = listenerMap.get(eventType);
        eventListenerList.remove(eventListener);
    }

    /**
     * 事件通知
     *
     * @param eventType 事件类型
     * @param result    事件数据
     */
    @Override
    public void notify(DanMuClientEventType eventType, DanMuClientEventResult result) {
        List<EventListener<DanMuClientEventResult>> eventListenerList = listenerMap.get(eventType);
        for (EventListener<DanMuClientEventResult> el :
                eventListenerList) {
            el.doEvent(result);
        }
    }
}
