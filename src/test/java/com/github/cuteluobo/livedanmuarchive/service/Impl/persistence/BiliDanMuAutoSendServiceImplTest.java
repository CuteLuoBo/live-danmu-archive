package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendTaskField;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.BiliDanMuSenderAccountData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import com.github.cuteluobo.livedanmuarchive.utils.BiliVideoUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class BiliDanMuAutoSendServiceImplTest {

    private static BiliDanMuAutoSendServiceImpl biliDanMuAutoSendService;

    @BeforeAll
    public static void setup() {
        //TODO CK删除
        BiliDanMuSenderAccountData accountData =
                new BiliDanMuSenderAccountData("");
        BiliDanMuSenderAccountData accountData2 = new BiliDanMuSenderAccountData("");
        List<BiliDanMuSenderAccountData> danMuSenderAccountDataList = new ArrayList<>(10);
        danMuSenderAccountDataList.add(accountData);
//        danMuSenderAccountDataList.add(accountData2);
        biliDanMuAutoSendService = BiliDanMuAutoSendServiceImpl.getInstance(danMuSenderAccountDataList);
    }

    @Test
    void startSendTask() throws ServiceException {
        BiliProcessedVideoData biliProcessedVideoData = BiliVideoUtil.matchVideo("BV1TP411W7Bu"
                ,ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getNormalValue(),
                ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getNormalValue(),
                null, null);
        biliDanMuAutoSendService.startSendTask(biliProcessedVideoData,"B站-甜药");
        while (true) {

        }
    }

    @Test
    void stopSendTask() throws ServiceException, InterruptedException {
        BiliProcessedVideoData biliProcessedVideoData = BiliVideoUtil.matchVideo("BV1TP411W7Bu"
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