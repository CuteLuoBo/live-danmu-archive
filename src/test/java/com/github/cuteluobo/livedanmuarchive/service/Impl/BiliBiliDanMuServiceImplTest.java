package com.github.cuteluobo.livedanmuarchive.service.Impl;

import com.github.cuteluobo.livedanmuarchive.enums.ExportPattern;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.service.Impl.parse.BiliBiliDanMuParseServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.persistence.SqliteDanMuExportServiceImpl;
import com.github.cuteluobo.livedanmuarchive.utils.BiliLoginUtil;
import com.github.cuteluobo.livedanmuarchive.utils.LinkUtil;
import com.github.cuteluobo.livedanmuarchive.websocketclient.BaseWebSocketClient;
import org.java_websocket.client.WebSocketClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * B站弹幕服务类测试
 *
 * @author: CuteLuoBo
 * @date: 2025/7/6  18:10
 * @version: 1.0.0
 */
class BiliBiliDanMuServiceImplTest {

    @Test
    @DisplayName("测试B站WS连接")
    @Disabled("手动启用测试")
    void testHandWebSocket() throws IOException, ServiceException, URISyntaxException, InterruptedException {
        //从环境变量中获取cookie
        String cookie = System.getenv("BILI_COOKIE");
        String testLiveRoomUrl = "https://live.bilibili.com/6";
        BiliBiliDanMuServiceImpl danmuService = new BiliBiliDanMuServiceImpl(testLiveRoomUrl, "ws链接测试"
                , new SqliteDanMuExportServiceImpl("test", ExportPattern.ALL_COLLECT)
                , null, BiliLoginUtil.getUserBaseInfoByCk(cookie)
        );
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(8),()->{
            WebSocketClient webSocketClient = new BaseWebSocketClient(new URI("wss://broadcastlv.chat.bilibili.com/sub"), LinkUtil.NORMAL_HEADER, 3600, 60, danmuService.getHeartbeatByteArray()
                    , new BiliBiliDanMuParseServiceImpl(new SqliteDanMuExportServiceImpl("test", ExportPattern.ALL_COLLECT)), danmuService.getWebsocketCmdByteList(), danmuService.getEventManager(), danmuService.getLiveRoomData());
            webSocketClient.connectBlocking();
            Thread.sleep(5*1000);
        });

    }
}