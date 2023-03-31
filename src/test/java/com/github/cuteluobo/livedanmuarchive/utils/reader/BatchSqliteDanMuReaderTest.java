package com.github.cuteluobo.livedanmuarchive.utils.reader;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.thread.ThreadException;
import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import org.junit.jupiter.api.BeforeAll;
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
        SqliteDanMuReader sqliteDanMuReader = new SqliteDanMuReader(new File("J:\\IDEA work-space\\huya-danmu-java\\export\\B站-甜药\\danmu\\【APEX】甜药--2023-03-01 14-53-44.db"));
        batchSqliteDanMuReader = new BatchSqliteDanMuReader(List.of(sqliteDanMuReader));
    }
    @Test
    void readListByPage() {
        DanMuDataModelSelector danMuDataModelSelector = new DanMuDataModelSelector(1678455306000L,1678458452000L);
        batchSqliteDanMuReader.readListByPage(danMuDataModelSelector, 0, 10).forEach(System.out::println);
    }

    @Test
    void readListByPageAsync() throws InterruptedException {
        ExecutorService poolExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("test", false));
        poolExecutor.submit(this::readListByPage);
        //须有线程保活才可正常执行
        Thread.sleep(10000);
    }
}