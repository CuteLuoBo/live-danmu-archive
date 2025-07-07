package com.github.cuteluobo.livedanmuarchive;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.github.cuteluobo.livedanmuarchive.builder.DanMuServiceBuilder;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuExportType;
import com.github.cuteluobo.livedanmuarchive.enums.ExportPattern;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.manager.FileExportManager;
import com.github.cuteluobo.livedanmuarchive.service.DanMuService;
import com.github.cuteluobo.livedanmuarchive.service.Impl.persistence.SqliteDanMuExportServiceImpl;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteJDBCLoader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * 弹幕相关测试类
 * @author CuteLuoBo
 * @date 2022/4/27 14:54
 */
public class TestUnitByDanMu extends TestCase {
    static FileExportManager fileExportManager;

    String sqliteTestSaveName = "test-DAY_FOLDER" + System.currentTimeMillis();
    String normalSqliteFileSuffix = ".db'";

    @BeforeClass
    public static void initTestPath() {
        fileExportManager = FileExportManager.getInstance(new File("test/export"));
    }

    /**
     * 测试数据库文件创建
     */
    @Test
    @DisplayName("测试数据库文件创建")
    public void testSQLiteServiceCreateByDayFolder() throws IOException {
        FileExportManager fileExportManager = FileExportManager.getInstance(new File("test/export"));
        SqliteDanMuExportServiceImpl sqliteDanMuExportService = new SqliteDanMuExportServiceImpl(sqliteTestSaveName, ExportPattern.DAY_FOLDER);
        File dataBasePath = new File(fileExportManager.getExportDir()
                + File.separator + sqliteTestSaveName + File.separator
                + SqliteDanMuExportServiceImpl.SAVE_DIR_NAME + File.separator);
        System.out.println("test dataBasePath: " + dataBasePath.getPath());
        Assert.assertTrue(dataBasePath.exists());
    }

    @Test
    @DisplayName("虎牙弹幕录制测试")
    @Disabled("手动测试")
    public void testHuya() throws Exception {
        System.out.println("当前模式: " +
                (SQLiteJDBCLoader.isNativeMode() ? "原生库" : "纯Java"));
        FileExportManager fileExportManager = FileExportManager.getInstance(new File("test/export"));
        DanMuServiceBuilder danMuServiceBuilder = new DanMuServiceBuilder("https://www.huya.com/lpl");
        danMuServiceBuilder.saveName("huya-lpl").danMuExportType(DanMuExportType.SQLITE).danMuExportPattern(ExportPattern.ALL_COLLECT);
        DanMuService danMuService = danMuServiceBuilder.build();
        //修改日志等级以输出解析结果
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("com.github.cuteluobo.livedanmuarchive.service.Impl.parse").setLevel(Level.TRACE);
        //测试5秒输出结果
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            danMuService.startRecord();
            Thread.sleep(5000);
        });

    }

    @Test
    @DisplayName("B站弹幕录制测试")
    @Disabled("手动测试")
    public void testBil() throws ServiceException, IOException, InterruptedException, URISyntaxException {
        FileExportManager fileExportManager = FileExportManager.getInstance(new File("test/export"));
        //修改日志等级以输出解析结果
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("com.github.cuteluobo.livedanmuarchive.service.Impl.parse").setLevel(Level.TRACE);
        loggerContext.getLogger("com.github.cuteluobo.livedanmuarchive.service.Impl").setLevel(Level.TRACE);
        DanMuServiceBuilder danMuServiceBuilder = new DanMuServiceBuilder("https://live.bilibili.com/6");
        danMuServiceBuilder.saveName("bil-lpl").danMuExportType(DanMuExportType.SQLITE).danMuExportPattern(ExportPattern.DAY_FOLDER);
        DanMuService danMuService = danMuServiceBuilder.build();
        //测试5秒输出结果
//        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
//            danMuService.startRecord();
//            Thread.sleep(5000);
//        });
        danMuService.startRecord();
        while (true) {
            Thread.sleep(10000);
        }
    }
}
