package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendTaskField;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.BiliDanMuSenderAccountData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import com.github.cuteluobo.livedanmuarchive.utils.BiliVideoUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class BiliDanMuAutoSendServiceImplTest {

    private static BiliDanMuAutoSendServiceImpl biliDanMuAutoSendService;

    @BeforeAll
    public static void setup() {
        //TODO CK删除
        //TODO 测试多个账号之间的协调工作
        BiliDanMuSenderAccountData accountData = new BiliDanMuSenderAccountData();
        accountData.setCookies("");
        biliDanMuAutoSendService = BiliDanMuAutoSendServiceImpl.getInstance(List.of(accountData));
    }

    @Test
    void startSendTask() throws ServiceException {
        BiliProcessedVideoData biliProcessedVideoData = BiliVideoUtil.matchVideo("BV1xL411y7jx"
                ,ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getNormalValue(),
                ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getNormalValue(),
                null, null);
        biliDanMuAutoSendService.startSendTask(biliProcessedVideoData,"B站-甜药");
        while (true) {

        }
    }

    @Test
    void stopSendTask() throws ServiceException, InterruptedException {
        BiliProcessedVideoData biliProcessedVideoData = BiliVideoUtil.matchVideo("BV1dL411178i"
                ,ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getNormalValue(),
                ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getNormalValue(),
                null, null);
        biliDanMuAutoSendService.startSendTask(biliProcessedVideoData,"B站-甜药");
        Thread.sleep(60 * 1000);
        biliDanMuAutoSendService.stopSendTask();
        while (true) {

        }
    }


}