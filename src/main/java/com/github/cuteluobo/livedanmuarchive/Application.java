package com.github.cuteluobo.livedanmuarchive;

import com.github.cuteluobo.livedanmuarchive.builder.DanMuServiceBuilder;
import com.github.cuteluobo.livedanmuarchive.controller.DanMuRecordController;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuExportPattern;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuExportType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.manager.DanMuClientEventManager;
import com.github.cuteluobo.livedanmuarchive.service.DanMuService;
import com.github.cuteluobo.livedanmuarchive.service.Impl.HuyaDanMuServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.SqliteDanMuExportServiceImpl;
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
        danMuRecordController.addTask("https://www.huya.com/kasha233", "虎牙-卡莎", DanMuExportType.SQLITE, DanMuExportPattern.ALL_COLLECT, 10L);
    }



}
