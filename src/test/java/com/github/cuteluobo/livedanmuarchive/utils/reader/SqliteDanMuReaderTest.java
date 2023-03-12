package com.github.cuteluobo.livedanmuarchive.utils.reader;

import cn.hutool.core.thread.NamedThreadFactory;
import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class SqliteDanMuReaderTest {
    private static SqliteDanMuReader sqliteDanMuReader;
    @BeforeAll
    public static void setUp() {
        sqliteDanMuReader = new SqliteDanMuReader(new File("J:\\IDEA work-space\\huya-danmu-java\\export\\B站-甜药\\danmu\\【APEX】甜药--2023-03-01 14-53-44.db"));
    }

    @Test
    void countNum() {
        System.out.println(sqliteDanMuReader.countNum(new DanMuDataModelSelector()));
    }

    @Test
    void readAll() {
        sqliteDanMuReader.readAll(new DanMuDataModelSelector()).forEach(System.out::println);
    }

    @Test
    void readListByPage() {
        DanMuDataModelSelector danMuDataModelSelector = new DanMuDataModelSelector(1678455306000L,1678458452000L);
        sqliteDanMuReader.readListByPage(danMuDataModelSelector, 0, 10).forEach(System.out::println);
    }
    @Test
    @Disabled
    void readListByPageAsync() throws InterruptedException {
        ExecutorService poolExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("test", false));
        poolExecutor.submit(this::readListByPage);
        Thread.sleep(10000);
    }

    @Test
    void readPage() {
    }

    @Test
    void conventList() {
    }

    @Test
    void getCreateTimeFirst() {
    }

    @Test
    void getOneById() {
    }

    @Test
    void getCreateTimeEnd() {
    }
}