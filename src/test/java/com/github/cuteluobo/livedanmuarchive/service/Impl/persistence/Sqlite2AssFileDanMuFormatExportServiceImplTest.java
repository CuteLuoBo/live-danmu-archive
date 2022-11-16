package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuExportDataInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Sqlite2AssFileDanMuFormatExportServiceImplTest {

    @Test
    @DisplayName("测试导出功能")
    public void testExport() throws ServiceException, IOException {
        List<File> fileList = List.of(new File("G:\\弹幕录制\\export\\B站-甜药\\danmu\\2022-11-14\\B站-甜药--2022-11-14 17-40-20.db"));
        long startTimeStamp = 1668418821L * 1000;
        long endTimeStamp = 1678418821L * 1000;
        File saveAssFilePath = new File("G:\\弹幕录制\\export\\B站-甜药\\danmu\\2022-11-14\\testExport");
        Sqlite2AssFileDanMuFormatExportServiceImpl service = new Sqlite2AssFileDanMuFormatExportServiceImpl("test", fileList, saveAssFilePath);
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
        Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData first = new Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData(0, "1", fontSize);

        //超出时间不会重叠
        Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData overTimeFirst = new Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData(showTime, "1", fontSize);
        assertFalse(Sqlite2AssFileDanMuFormatExportServiceImpl.checkDanMuOverlap(screenWidth, first, overTimeFirst, showTime));
        //在时间内但不会重叠
        Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData innerTimeFirst = new Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData(5000, "1", fontSize);
        assertFalse(Sqlite2AssFileDanMuFormatExportServiceImpl.checkDanMuOverlap(screenWidth, first, innerTimeFirst, showTime));
        //长内容，在时间内但不会重叠
        Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData test3 = new Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData(5000, "12345678910111213", fontSize);
        assertFalse(Sqlite2AssFileDanMuFormatExportServiceImpl.checkDanMuOverlap(screenWidth, first, test3, showTime));
        //长内容，在时间内会重叠
        Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData test4 = new Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData(0, "123456789", fontSize);
        Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData test5 = new Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData(1000, "1234567891011121314151617181920", fontSize);
        assertTrue(Sqlite2AssFileDanMuFormatExportServiceImpl.checkDanMuOverlap(screenWidth, test4, test5, showTime));
    }
}