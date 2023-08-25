package com.github.cuteluobo.livedanmuarchive.async;

import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendTaskField;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.BiliDanMuSenderAccountData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuUserInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import com.github.cuteluobo.livedanmuarchive.utils.BiliVideoUtil;
import com.github.cuteluobo.livedanmuarchive.utils.reader.BatchSqliteDanMuReader;
import com.github.cuteluobo.livedanmuarchive.utils.reader.SqliteDanMuReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BiliDanMuSenderTest {
    private static BiliDanMuSender danMuSender;

    @Test
    @BeforeAll
    public static  void setUp() {
        BiliDanMuSenderAccountData accountData = new BiliDanMuSenderAccountData();
        accountData.setUid("test");
        accountData.setCookies("");
        SqliteDanMuReader sqliteDanMuReader = new SqliteDanMuReader(new File("J:\\IDEA work-space\\huya-danmu-java\\export\\B站-甜药\\danmu\\【APEX】甜药--2023-03-01 14-53-44.db"));
        BatchSqliteDanMuReader batchSqliteDanMuReader = new BatchSqliteDanMuReader(List.of(sqliteDanMuReader));
        danMuSender = new BiliDanMuSender(accountData,batchSqliteDanMuReader);
    }

    @Test
    void createRenewTask() {
    }

    @Test
    void createTask() throws ServiceException {
        BiliProcessedVideoData biliProcessedVideoData = BiliVideoUtil.matchVideo("BV1xL411y7jx"
                , ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getNormalValue(),
                ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getNormalValue(),
                null, null);
        danMuSender.createTask(biliProcessedVideoData).run();
    }


}