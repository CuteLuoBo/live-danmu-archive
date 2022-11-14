package com.github.cuteluobo.livedanmuarchive;

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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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
    public void testSQLiteServiceCreateByDayFolder() throws IOException {
        FileExportManager fileExportManager = FileExportManager.getInstance(new File("test/export"));
        SqliteDanMuExportServiceImpl sqliteDanMuExportService = new SqliteDanMuExportServiceImpl(sqliteTestSaveName, ExportPattern.DAY_FOLDER);
        File dataBasePath = new File(fileExportManager.getExportDir()
                + File.separator + sqliteTestSaveName + File.separator
                + SqliteDanMuExportServiceImpl.SAVE_DIR_NAME + File.separator);
        System.out.println("test dataBasePath: " + dataBasePath.getPath());
        Assert.assertTrue(dataBasePath.exists());

//        File dataBaseFile = new File(dataBasePath.getAbsolutePath() + File.separator
//                + LocalDateTime.now().format(sqliteDanMuExportService.getNormalDateFormatter()) + File.separator
//                + sqliteDanMuExportService.getSaveFileName() + normalSqliteFileSuffix);
//        System.out.println("test dataBaseFile: " + dataBaseFile.getPath());
//        Assert.assertTrue(dataBaseFile.exists());

    }

    @Test
    public void testHuya() throws ServiceException, IOException, InterruptedException, URISyntaxException {
        FileExportManager fileExportManager = FileExportManager.getInstance(new File("test/export"));
        DanMuServiceBuilder danMuServiceBuilder = new DanMuServiceBuilder("https://www.huya.com/lpl");
        danMuServiceBuilder.saveName("huya-lpl").danMuExportType(DanMuExportType.SQLITE).danMuExportPattern(ExportPattern.ALL_COLLECT);
        DanMuService danMuService = danMuServiceBuilder.build();
        danMuService.startRecord();
    }

    @Test
    public void testBil() throws ServiceException, IOException, InterruptedException, URISyntaxException {
        FileExportManager fileExportManager = FileExportManager.getInstance(new File("test/export"));
        DanMuServiceBuilder danMuServiceBuilder = new DanMuServiceBuilder("https://live.bilibili.com/6");
        danMuServiceBuilder.saveName("bil-lpl").danMuExportType(DanMuExportType.SQLITE).danMuExportPattern(ExportPattern.ALL_COLLECT);
        DanMuService danMuService = danMuServiceBuilder.build();
        danMuService.startRecord();
    }
}
