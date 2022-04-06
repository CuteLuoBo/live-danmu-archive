package com.github.cuteluobo.livedanmuarchive;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuExportPattern;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.service.DanMuService;
import com.github.cuteluobo.livedanmuarchive.service.Impl.ConsoleDanMuExportServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.HuyaDanMuServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.JsonDanMuExportServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.SqliteDanMuExportServiceImpl;
import com.qq.tars.protocol.tars.TarsInputStream;
import com.qq.tars.protocol.tars.TarsOutputStream;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 63541
 */
@SpringBootApplication
public class Application {


    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, ServiceException {
        //TODO 直接使用spring启动时，tar会报错，后续考虑取消依赖spring/fastStart包
//        SpringApplication.run(Application.class, args);
//        DanMuService danMuService = new HuyaDanMuServiceImpl("kaerlol");
//        danMuService.startRecord(new SqliteDanMuExportServiceImpl("虎牙-卡尔", DanMuExportPattern.DAY_FOLDER));
            DanMuService danMuService = new HuyaDanMuServiceImpl("518518");
            danMuService.startRecord(new SqliteDanMuExportServiceImpl("Letme严君泽", DanMuExportPattern.ALL_COLLECT));
    }



}
