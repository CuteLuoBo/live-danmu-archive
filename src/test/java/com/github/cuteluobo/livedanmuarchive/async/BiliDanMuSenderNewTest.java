package com.github.cuteluobo.livedanmuarchive.async;

import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendTaskField;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.model.DanMuTaskPlanModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedPartVideoData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import com.github.cuteluobo.livedanmuarchive.utils.BiliVideoUtil;
import com.github.cuteluobo.livedanmuarchive.utils.reader.BatchSqliteDanMuReader;
import com.github.cuteluobo.livedanmuarchive.utils.reader.SqliteDanMuReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>描 述：</p>
 *
 * @author: CuteLuoBo
 * @date: 2025/7/10  19:49
 * @version: 1.0.0
 */
class BiliDanMuSenderNewTest {
    BiliProcessedVideoData biliProcessedVideoData;
    BiliDanMuSenderNew biliDanMuSenderNew;
    BiliProcessedPartVideoData bodyPartVideoData;
    @BeforeEach
     void setUp() throws ServiceException {
         biliDanMuSenderNew = new BiliDanMuSenderNew(new BatchSqliteDanMuReader(
                 new ArrayList<>(List.of(
                         new SqliteDanMuReader(new File("J:\\IDEA work-space\\huya-danmu-java\\export\\B站-甜药\\danmu\\2025-07-07\\B站-甜药--2025-07-07 18-31-17.db"))
                 ))
         ));
         biliProcessedVideoData = BiliVideoUtil.matchVideo("BV1AJ32zjEEN"
                 , ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getNormalValue(),
                 ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getNormalValue(),
                 null, null);
        bodyPartVideoData = biliProcessedVideoData.getPartVideoDataList().get(11);
     }

    @Test
    void runSender() throws InterruptedException {
        biliDanMuSenderNew.runSender(biliProcessedVideoData);
//        while (true){
//            TimeUnit.SECONDS.sleep(5);
//        }
    }

    @Test
    void saveTaskPlan() {
        biliDanMuSenderNew.saveTaskPlan(biliDanMuSenderNew.createDanmuAccountTaskModel(bodyPartVideoData));
    }

    @Test
    void getSendDanMuList() {
        DanMuTaskPlanModel danMuTaskPlanModel = new DanMuTaskPlanModel();
        danMuTaskPlanModel.setPageCurrent(0);
        List<DanMuData> list = biliDanMuSenderNew.getSendDanMuList(bodyPartVideoData, danMuTaskPlanModel);
        System.out.println("size:" + list.size());
        list.forEach(System.out::println);
    }

    @Test
    void getHistoryTask() {
        DanMuTaskPlanModel danMuTaskPlanModel = biliDanMuSenderNew.getHistoryTask(bodyPartVideoData);
    }

    @Test
    void createDanmuAccountTaskModel() {
        DanMuTaskPlanModel danMuTaskPlanModel = biliDanMuSenderNew.createDanmuAccountTaskModel(bodyPartVideoData);
    }
}