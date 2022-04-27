package com.github.cuteluobo.livedanmuarchive.listener.result;

import com.github.cuteluobo.livedanmuarchive.pojo.LiveRoomData;

/**
 * 弹幕客户端事件结果类
 *
 * @author CuteLuoBo
 * @date 2022/4/7 19:14
 */
public class DanMuClientEventResult extends EventResult {
    /**
     * 当前直播名称
     */
    private LiveRoomData liveRoomData;

    /**
     * WS连接是否关闭
     */
    private Boolean isWebsocketConnectClose = false;

    public LiveRoomData getLiveRoomData() {
        return liveRoomData;
    }

    public void setLiveRoomData(LiveRoomData liveRoomData) {
        this.liveRoomData = liveRoomData;
    }

    public Boolean getWebsocketConnectClose() {
        return isWebsocketConnectClose;
    }

    public void setWebsocketConnectClose(Boolean websocketConnectClose) {
        isWebsocketConnectClose = websocketConnectClose;
    }
}
