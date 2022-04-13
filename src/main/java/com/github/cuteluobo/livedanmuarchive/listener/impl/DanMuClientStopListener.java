package com.github.cuteluobo.livedanmuarchive.listener.impl;

import com.github.cuteluobo.livedanmuarchive.controller.DanMuRecordController;
import com.github.cuteluobo.livedanmuarchive.listener.EventListener;
import com.github.cuteluobo.livedanmuarchive.listener.result.DanMuClientEventResult;
import com.github.cuteluobo.livedanmuarchive.pojo.LiveRoomData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 弹幕客户端中断监听类
 * @author CuteLuoBo
 * @date 2022/4/8 10:16
 */
public class DanMuClientStopListener implements EventListener<DanMuClientEventResult> {
    Logger logger = LoggerFactory.getLogger(DanMuClientStopListener.class);

    /**
     * 接收到WS客户端中断消息时，触发尝试重新执行任务
     *
     * @param eventResult 事件结果
     */
    @Override
    public void doEvent(DanMuClientEventResult eventResult) {
        DanMuRecordController danMuRecordController = DanMuRecordController.getInstance();
        LiveRoomData liveRoomData = eventResult.getLiveRoomData();
        danMuRecordController.restartTask(liveRoomData.getWebsiteType(),liveRoomData.getLiveRoomCode());
    }
}
