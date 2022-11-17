package com.github.cuteluobo.livedanmuarchive.service.Impl;

import com.github.cuteluobo.livedanmuarchive.command.base.CommandCenter;
import com.github.cuteluobo.livedanmuarchive.controller.DanMuRecordController;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuExportType;
import com.github.cuteluobo.livedanmuarchive.enums.ExportPattern;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.service.Impl.parse.DouyuDanMuParseServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.persistence.SqliteDanMuExportServiceImpl;
import com.github.cuteluobo.livedanmuarchive.utils.LinkUtil;
import com.github.cuteluobo.livedanmuarchive.websocketclient.BaseWebSocketClient;
import org.java_websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

class DouyuDanMuServiceImplTest {

    @Test
    void startRecord() throws IOException, ServiceException, URISyntaxException, InterruptedException {
//        System.setProperty("http.proxyHost", "127.0.0.1");
//        System.setProperty("https.proxyHost", "127.0.0.1");
//        System.setProperty("http.proxyPort", "8888");
//        System.setProperty("https.proxyPort", "8888");
//        System.setProperty("javax.net.ssl.trustStoreType","Windows-ROOT");
        String testLiveRoomUrl = "https://www.douyu.com/99999";
        String saveName = "斗鱼-旭旭宝宝";
        DouyuDanMuServiceImpl douyuDanMuService = new DouyuDanMuServiceImpl(testLiveRoomUrl, "斗鱼-旭旭宝宝"
                , new SqliteDanMuExportServiceImpl("旭旭宝宝", ExportPattern.ALL_COLLECT)
                , null
        );
        douyuDanMuService.startRecord();
//        DanMuRecordController danMuRecordController = DanMuRecordController.getInstance();
//        danMuRecordController.addTask(testLiveRoomUrl,saveName, DanMuExportType.SQLITE,ExportPattern.ALL_COLLECT,5000L);
    }

    @Test
    void useController() throws ServiceException, IOException, URISyntaxException {
//        System.setProperty("http.proxyHost", "127.0.0.1");
//        System.setProperty("https.proxyHost", "127.0.0.1");
//        System.setProperty("http.proxyPort", "8888");
//        System.setProperty("https.proxyPort", "8888");
//        System.setProperty("javax.net.ssl.trustStoreType","Windows-ROOT");
        String testLiveRoomUrl = "https://www.douyu.com/99999";
        String saveName = "斗鱼-邦Sa";
        DanMuRecordController danMuRecordController = DanMuRecordController.getInstance();
        danMuRecordController.addTask(testLiveRoomUrl,saveName, DanMuExportType.SQLITE,ExportPattern.ALL_COLLECT,5000L);
        CommandCenter.INSTANCE.startCommandListen();
    }

    @Test
    void testHandWebSocket() throws IOException, ServiceException, URISyntaxException, InterruptedException {
        String testLiveRoomUrl = "https://www.douyu.com/99999";
        String saveName = "斗鱼-旭旭宝宝";
        DouyuDanMuServiceImpl douyuDanMuService = new DouyuDanMuServiceImpl(testLiveRoomUrl, "斗鱼-旭旭宝宝"
                , new SqliteDanMuExportServiceImpl("旭旭宝宝", ExportPattern.ALL_COLLECT)
                , null
        );
        WebSocketClient webSocketClient = new BaseWebSocketClient(new URI("wss://danmuproxy.douyu.com:8503/"), LinkUtil.NORMAL_HEADER, 3600, 60, douyuDanMuService.getHeartbeatByteArray()
                , new DouyuDanMuParseServiceImpl(new SqliteDanMuExportServiceImpl("旭旭宝宝", ExportPattern.ALL_COLLECT)), douyuDanMuService.getWebsocketCmdByteList(), douyuDanMuService.getEventManager(), douyuDanMuService.getLiveRoomData());
        webSocketClient.connectBlocking();

    }
}