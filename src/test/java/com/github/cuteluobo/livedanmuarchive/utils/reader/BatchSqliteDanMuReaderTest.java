package com.github.cuteluobo.livedanmuarchive.utils.reader;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.thread.ThreadException;
import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

class BatchSqliteDanMuReaderTest {
    private static BatchSqliteDanMuReader batchSqliteDanMuReader;
    @BeforeAll
    public static void setUp() {
        SqliteDanMuReader sqliteDanMuReader = new SqliteDanMuReader(new File("J:\\IDEA work-space\\huya-danmu-java\\export\\B站-甜药\\danmu\\B站-甜药--2023-07-17 12-17-54.db"));
        batchSqliteDanMuReader = new BatchSqliteDanMuReader(List.of(sqliteDanMuReader));
    }
    @Test
    @DisplayName("获取指定时间段的弹幕列表")
    @Disabled("手动指定文件测试")
    void readListByPage() {
        DanMuDataModelSelector danMuDataModelSelector = new DanMuDataModelSelector(1689567695687L,1689571702911L);
//        batchSqliteDanMuReader.readListByPage(danMuDataModelSelector, 0, 10).forEach(System.out::println);
        assertFalse(batchSqliteDanMuReader.readListByPage(danMuDataModelSelector, 0, 10).isEmpty());
    }
}