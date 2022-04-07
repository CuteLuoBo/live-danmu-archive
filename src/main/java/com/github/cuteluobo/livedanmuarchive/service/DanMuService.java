package com.github.cuteluobo.livedanmuarchive.service;

import com.github.cuteluobo.livedanmuarchive.enums.IOWriteType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * 弹幕服务类
 * @author CuteLuoBo
 * @date 2021/12/16 16:13
 */
public interface DanMuService {
    /**
     * 开始录制
     * @param danMuExportService 使用的导出接口
     * @throws URISyntaxException URI解析错误
     */
    void startRecord(DanMuExportService danMuExportService) throws URISyntaxException, InterruptedException, ServiceException, IOException;


}
