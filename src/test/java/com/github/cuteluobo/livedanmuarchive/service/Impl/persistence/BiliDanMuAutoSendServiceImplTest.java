package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendTaskField;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.BiliDanMuSenderAccountData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import com.github.cuteluobo.livedanmuarchive.utils.BiliVideoUtil;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

class BiliDanMuAutoSendServiceImplTest {

    private static BiliDanMuAutoSendServiceImpl biliDanMuAutoSendService;

    @BeforeAll
    public static void setup() {
        //从环境变量中获取cookie
        String cookie = System.getenv("BILI_COOKIE");
        BiliDanMuSenderAccountData accountData = new BiliDanMuSenderAccountData(cookie);
        List<BiliDanMuSenderAccountData> danMuSenderAccountDataList = new ArrayList<>(10);
        danMuSenderAccountDataList.add(accountData);
        biliDanMuAutoSendService = BiliDanMuAutoSendServiceImpl.getInstance(danMuSenderAccountDataList);
    }

//    @Test
    @DisplayName("服务类测试-发送指定稿件弹幕")
    @Disabled
    void startSendTask() throws ServiceException {
        BiliProcessedVideoData biliProcessedVideoData = BiliVideoUtil.matchVideo("BV1TP411W7Bu"
                ,ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getNormalValue(),
                ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getNormalValue(),
                null, null);
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            biliDanMuAutoSendService.startSendTask(biliProcessedVideoData,"B站-甜药");
            Thread.sleep(3 * 1000L);
        });
    }

//    @Test
    @DisplayName("服务类测试-发送指定稿件弹幕并手动终止")
    @Disabled
    void stopSendTask() throws ServiceException{
        BiliProcessedVideoData biliProcessedVideoData = BiliVideoUtil.matchVideo("BV1TP411W7Bu"
                ,ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getNormalValue(),
                ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getNormalValue(),
                null, null);
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            biliDanMuAutoSendService.startSendTask(biliProcessedVideoData,"B站-甜药");
            Thread.sleep(5 * 1000L);
            biliDanMuAutoSendService.stopSendTask();
        });
    }


}