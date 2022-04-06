package com.github.cuteluobo.livedanmuarchive.utils;

import com.github.cuteluobo.livedanmuarchive.websocketclient.BaseWebSocketClient;
import lombok.SneakyThrows;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

/**
 * ws分线程定时器
 * @author CuteLuoBo
 * @date 2021/12/17 16:33
 */
public class WebSocketInterval implements Runnable {
    Logger logger = LoggerFactory.getLogger(WebSocketInterval.class);
    private BaseWebSocketClient webSocketClient;

    public WebSocketInterval (BaseWebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }
    /**
     * 定时调用发送心跳包
     */
    @SneakyThrows
    @Override
    public void run() {
        webSocketClient.intervalSendData();
        logger.debug("触发ws客户端定时发送");
    }
}
