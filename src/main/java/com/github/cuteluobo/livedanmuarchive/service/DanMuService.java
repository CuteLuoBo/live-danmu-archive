package com.github.cuteluobo.livedanmuarchive.service;

import com.github.cuteluobo.livedanmuarchive.enums.IOWriteType;
import com.github.cuteluobo.livedanmuarchive.enums.WebsiteType;
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
     * 获取直播Url
     * @return 直播Url
     */
    String getLiveRoomUrl();

    /**
     * 获取服务支持平台类型
     * @return 服务支持平台类型
     */
    WebsiteType getServiceSupportWebsiteType();

    /**
     * 获取直播间代号
     * @return 直播间代号
     */
    String getLiveRoomCode();

    /**
     * 获取直播主播名称
     * @return 直播主播名称
     */
    String getLiveAnchorName();

    /**
     * 开始录制--自定义接口
     * @param danMuExportService 使用的导出接口
     * @throws InterruptedException 线程错误
     * @throws ServiceException     服务错误
     * @throws IOException          URL/IO错误
     * @throws URISyntaxException URI解析错误
     */
    void startRecord(DanMuExportService danMuExportService) throws URISyntaxException, InterruptedException, ServiceException, IOException;

    /**
     * 开始录制
     * @throws URISyntaxException URI解析错误
     * @throws InterruptedException 线程错误
     * @throws ServiceException 服务错误
     * @throws IOException URL/IO错误
     */
    void startRecord() throws URISyntaxException, InterruptedException, ServiceException, IOException;
}
