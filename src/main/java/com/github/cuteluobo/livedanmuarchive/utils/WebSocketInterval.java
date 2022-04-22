package com.github.cuteluobo.livedanmuarchive.utils;

import com.github.cuteluobo.livedanmuarchive.websocketclient.BaseWebSocketClient;
import com.github.cuteluobo.livedanmuarchive.websocketclient.IntervalRun;
import lombok.SneakyThrows;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * ws分线程定时器
 * @author CuteLuoBo
 * @date 2021/12/17 16:33
 */
public class WebSocketInterval implements Runnable {
    Logger logger = LoggerFactory.getLogger(WebSocketInterval.class);
    private IntervalRun intervalRun;
    /**
     * 定时时间
     */
    private int intervalTime = 60;

    public WebSocketInterval (IntervalRun intervalRun) {
        this.intervalRun = intervalRun;
    }

    public WebSocketInterval (IntervalRun intervalRun,Integer intervalTime) {
        this.intervalRun = intervalRun;
        if (intervalTime != null && intervalTime > 0) {
            this.intervalTime = intervalTime;
        }
    }
    /**
     * 定时调用发送心跳包
     */
    @SneakyThrows
    @Override
    public void run() {
        logger.debug("触发定时操作");
        intervalRun.intervalRun();
    }

    public IntervalRun getIntervalRun() {
        return intervalRun;
    }

    public void setIntervalRun(IntervalRun intervalRun) {
        this.intervalRun = intervalRun;
    }

    public int getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }
}
