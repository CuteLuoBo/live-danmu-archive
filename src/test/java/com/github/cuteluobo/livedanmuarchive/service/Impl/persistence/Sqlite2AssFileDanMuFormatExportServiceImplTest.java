package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuExportDataInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.FormatDanMuData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Sqlite2AssFileDanMuFormatExportServiceImplTest {

    @Test
    @DisplayName("手动测试-导出功能")
    @Disabled("手动启用")
    public void testExport() throws ServiceException, IOException {
        List<File> fileList = List.of(new File("G:\\弹幕录制\\export\\B站-甜药\\danmu\\2022-11-14\\B站-甜药--2022-11-14 17-40-20.db"));
        long startTimeStamp = 1668418821L * 1000;
        long endTimeStamp = 1678418821L * 1000;
        File saveAssFilePath = new File("G:\\弹幕录制\\export\\B站-甜药\\danmu\\2022-11-14\\testExport");
        Sqlite2AssFileDanMuFormatExportServiceImpl service = new Sqlite2AssFileDanMuFormatExportServiceImpl(fileList, "test", saveAssFilePath);
//        service.formatExportAll();
        DanMuExportDataInfo<File> danMuExportDataInfo = service.formatExportBySelector(LocalDateTime.ofEpochSecond(startTimeStamp / 1000, 0, OffsetDateTime.now().getOffset())
                , LocalDateTime.ofEpochSecond(endTimeStamp / 1000, 0, OffsetDateTime.now().getOffset()));
        assertNotNull(danMuExportDataInfo);
        assertNotNull(danMuExportDataInfo.getData());
        System.out.println("导出文件路径:"+danMuExportDataInfo.getData().getAbsolutePath());
    }

    @Test
    @DisplayName("测试弹幕覆盖判断")
    public void checkDanMuOverlap() {
        int screenWidth = 1920;
        int showTime = 8000;
        int fontSize = 25;
        int allowSpace = 25;
        FormatDanMuData first = new FormatDanMuData(0, "1", fontSize);
        //超出时间不会重叠
        FormatDanMuData overTimeFirst = new FormatDanMuData(showTime, "1", fontSize);
        assertFalse(SqliteDanMuFormatExportService.checkDanMuOverlap(screenWidth, first, overTimeFirst, allowSpace, showTime));
        //在时间内但不会重叠
        FormatDanMuData innerTimeFirst = new FormatDanMuData(5000, "1", fontSize);
        assertFalse(SqliteDanMuFormatExportService.checkDanMuOverlap(screenWidth, first, innerTimeFirst, allowSpace, showTime));
        //长内容，在时间内但不会重叠
        FormatDanMuData test3 = new FormatDanMuData(5000, "12345678910111213", fontSize);
        assertFalse(SqliteDanMuFormatExportService.checkDanMuOverlap(screenWidth, first, test3, allowSpace, showTime));
        //长内容，在时间内会重叠
        FormatDanMuData test4 = new FormatDanMuData(0, "123456789", fontSize);
        FormatDanMuData test5 = new FormatDanMuData(1000, "1234567891011121314151617181920", fontSize);
        assertTrue(SqliteDanMuFormatExportService.checkDanMuOverlap(screenWidth, test4, test5, allowSpace, showTime));
    }
}