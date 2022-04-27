package com.github.cuteluobo.livedanmuarchive;

import com.github.cuteluobo.livedanmuarchive.controller.DanMuRecordController;
import com.github.cuteluobo.livedanmuarchive.enums.ExportPattern;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuExportType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.service.Impl.BiliBiliDanMuParseServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.BiliBiliDanMuServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.SqliteDanMuExportServiceImpl;
import com.github.cuteluobo.livedanmuarchive.utils.CustomConfigUtil;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * 主程序入口
 * @author CuteLuoBo
 */
public class Application {

    public static void main(String[] args){
        //初始化控制器并读取任务配置文件
        DanMuRecordController danMuRecordController = DanMuRecordController.getInstance();
        danMuRecordController.addTaskByNormalConfigFile();
    }

}
