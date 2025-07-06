package com.github.cuteluobo.livedanmuarchive.utils.reader;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import cn.hutool.core.thread.NamedThreadFactory;
import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteJDBCLoader;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class SqliteDanMuReaderTest {
    private static SqliteDanMuReader sqliteDanMuReader;
    @BeforeAll
    public static void setUp() {
        sqliteDanMuReader = new SqliteDanMuReader(new File("J:\\IDEA work-space\\huya-danmu-java\\export\\B站-甜药\\danmu\\B站-甜药--2023-07-17 12-17-54.db"));
    }

    @Test
    @DisplayName("获取弹幕总量")
    @Disabled("手动指定文件测试")
    void countNum() {
        System.out.println(sqliteDanMuReader.countNum(new DanMuDataModelSelector()));
    }

    @Test
    @DisplayName("获取指定时间段的弹幕列表")
    @Disabled("手动指定文件测试")
    void readListByPage() {
        DanMuDataModelSelector danMuDataModelSelector = new DanMuDataModelSelector(1689567695687L,1689571702911L);
        assertFalse(sqliteDanMuReader.readListByPage(danMuDataModelSelector, 0, 10).isEmpty());
    }

    @Test
    @DisplayName("获取指定时间段的弹幕列表")
    @Disabled("手动指定文件测试")
    void readPageListByTime() throws Exception {
        Long startTime = 1689567695687L;
        DanMuDataModelSelector danMuDataModelSelector = new DanMuDataModelSelector(startTime,startTime+ 3600L * 1000L);
        List<DanMuData> danMuDataModelList = sqliteDanMuReader.readListByTime(danMuDataModelSelector, 1, 20 * 1000);
        assertFalse(danMuDataModelList.isEmpty());
        danMuDataModelList.forEach(System.out::println);
    }
}