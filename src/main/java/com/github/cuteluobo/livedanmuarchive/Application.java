package com.github.cuteluobo.livedanmuarchive;

import com.github.cuteluobo.livedanmuarchive.controller.DanMuRecordController;
import com.github.cuteluobo.livedanmuarchive.enums.ExportPattern;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuExportType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.service.Impl.BiliBiliDanMuParseServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.BiliBiliDanMuServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.SqliteDanMuExportServiceImpl;
import com.github.cuteluobo.livedanmuarchive.utils.CustomConfigUtil;
import org.glassfish.tyrus.core.TyrusWebSocketEngine;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author 63541
 */
@SpringBootApplication
public class Application {


    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, ServiceException {
        //TODO 直接使用spring启动时，tar会报错，后续考虑取消依赖spring/fastStart包
//        SpringApplication.run(Application.class, args);
        DanMuRecordController danMuRecordController = DanMuRecordController.getInstance();
//        danMuRecordController.addTask("https://www.huya.com/kasha233", "虎牙-卡莎", DanMuExportType.SQLITE, ExportPattern.ALL_COLLECT, 10L);
//        danMuRecordController.addTaskByNormalConfigFile();
//        String saveName = "B站-小琨爱小蛊";
//        String url = "https://live.bilibili.com/2930352?session_id=fe9e77d4d6befaab8280a2f137caff3d_038656CB-CBB1-4584-8455-FF7270BE99C1&launch_id=1000216";
//        String saveName = "B站-甜药";
//        String url = "https://live.bilibili.com/13308358?broadcast_type=0&visit_id=61in878sdsk0";
//        String saveName = "B站-晓晨Official";
//        String url = "https://live.bilibili.com/671817";
//        String saveName = "B站-7777";
//        String url = "https://live.bilibili.com/7777";
//        String saveName = "B站-Elysian绿豆";
//        String url = "https://live.bilibili.com/26445?extra_jump_from=28011&from=28011&hotRank=1";
        String saveName = "B站-两仪滚";
        String url = "https://live.bilibili.com/388";
        BiliBiliDanMuServiceImpl biliBiliDanMuService = new BiliBiliDanMuServiceImpl(url, saveName, new SqliteDanMuExportServiceImpl(saveName, ExportPattern.ALL_COLLECT), null);
        biliBiliDanMuService.startRecord();
    }




}
