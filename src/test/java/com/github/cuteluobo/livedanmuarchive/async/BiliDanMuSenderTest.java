package com.github.cuteluobo.livedanmuarchive.async;

import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendTaskField;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.BiliDanMuSenderAccountData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuUserInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseUserInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import com.github.cuteluobo.livedanmuarchive.service.Impl.persistence.BiliDanMuAutoSendServiceImpl;
import com.github.cuteluobo.livedanmuarchive.utils.BiliLoginUtil;
import com.github.cuteluobo.livedanmuarchive.utils.BiliVideoUtil;
import com.github.cuteluobo.livedanmuarchive.utils.reader.BatchSqliteDanMuReader;
import com.github.cuteluobo.livedanmuarchive.utils.reader.SqliteDanMuReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BiliDanMuSenderTest {
    private static BiliDanMuSender danMuSender;

    @BeforeAll
    public static  void setUp() throws URISyntaxException {
        BiliDanMuSenderAccountData accountData = new BiliDanMuSenderAccountData();
        accountData.setUid("test");
        accountData.setCookies(null);
        //TODO 删除CK
        accountData.setAccessKey("");
        accountData.setAppKey("4409e2ce8ffd12b8");
        SqliteDanMuReader sqliteDanMuReader = new SqliteDanMuReader(new File("J:\\IDEA work-space\\huya-danmu-java\\export\\B站-甜药\\danmu\\B站-甜药--2023-07-17 12-17-54.db"));
        BatchSqliteDanMuReader batchSqliteDanMuReader = new BatchSqliteDanMuReader(List.of(sqliteDanMuReader));
        BiliDanMuAutoSendServiceImpl biliDanMuAutoSendService = BiliDanMuAutoSendServiceImpl.getInstance(new ArrayList<>());
        assertTrue(Optional.of(BiliLoginUtil.getUserBaseInfoByAppKey(accountData.getAccessKey(), accountData.getAppKey(), accountData.getAppSec())).map(BaseUserInfo::getUid).orElse(-1L) > 0);
        danMuSender = new BiliDanMuSender(accountData,batchSqliteDanMuReader);
    }


    @Test
    @DisplayName("手动测试发送稿件弹幕功能")
    @Disabled
    void createTask() throws ServiceException {
        BiliProcessedVideoData biliProcessedVideoData = BiliVideoUtil.matchVideo("BV1eh4y1D7EC"
                , ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getNormalValue(),
                ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getNormalValue(),
                null, null);
        assertTimeoutPreemptively(Duration.ofSeconds(5L), () -> {
            danMuSender.createTask(biliProcessedVideoData).run();
            Thread.sleep(3 * 1000L);
        });
    }


}