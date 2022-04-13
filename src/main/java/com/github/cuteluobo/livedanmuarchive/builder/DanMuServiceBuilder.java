package com.github.cuteluobo.livedanmuarchive.builder;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuClientEventType;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuExportPattern;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuExportType;
import com.github.cuteluobo.livedanmuarchive.enums.WebsiteType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.listener.result.DanMuClientEventResult;
import com.github.cuteluobo.livedanmuarchive.manager.EventManager;
import com.github.cuteluobo.livedanmuarchive.service.DanMuExportService;
import com.github.cuteluobo.livedanmuarchive.service.DanMuService;
import com.github.cuteluobo.livedanmuarchive.service.Impl.HuyaDanMuServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.JsonDanMuExportServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.SqliteDanMuExportServiceImpl;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 弹幕服务组装类
 * @author CuteLuoBo
 * @date 2022/4/12 12:01
 */
public class DanMuServiceBuilder {
    private WebsiteType websiteType;
    private DanMuExportType danMuExportType = DanMuExportType.SQLITE;
    private DanMuExportPattern danMuExportPattern = DanMuExportPattern.ALL_COLLECT;
    private String liveUrl;
    private String saveName = "new save-"+System.currentTimeMillis();
    private EventManager<DanMuClientEventType, DanMuClientEventResult> danMuClientEventManager = null;

    /**
     * 直播平台url匹配
     */
    public static final Pattern LIVE_TYPE_PATTERN = Pattern.compile("//(\\S+\\.)?(\\S+)\\.");

    public DanMuServiceBuilder(String liveUrl) throws ServiceException {
        this.liveUrl = liveUrl;
        checkLiveWebsiteType(liveUrl);
    }


    public DanMuServiceBuilder danMuExportType(DanMuExportType danMuExportType) {
        this.danMuExportType = danMuExportType;
        return this;
    }

    public DanMuServiceBuilder danMuExportPattern(DanMuExportPattern danMuExportPattern) {
        this.danMuExportPattern = danMuExportPattern;
        return this;
    }

    public DanMuServiceBuilder saveName(String saveName) {
        this.saveName = saveName;
        return this;
    }

    public DanMuServiceBuilder websiteType(WebsiteType websiteType) {
        this.websiteType = websiteType;
        return this;
    }

    public DanMuServiceBuilder danMuClientEventManager(EventManager<DanMuClientEventType, DanMuClientEventResult> danMuClientEventManager) {
        this.danMuClientEventManager = danMuClientEventManager;
        return this;
    }

    /**
     * 校验URL与直播平台
     * @param liveUrl 直播链接
     * @throws ServiceException
     */
    private void checkLiveWebsiteType(String liveUrl) throws ServiceException {
        Matcher matcher = LIVE_TYPE_PATTERN.matcher(liveUrl);
        if (matcher.find()) {
            String liveType = matcher.group(2);
            WebsiteType websiteType = WebsiteType.getEnumByValue(liveType);
            if (websiteType == null) {
                throw new ServiceException("识别的直播平台不支持："+liveType);
            }
            this.websiteType = websiteType;
        } else {
            throw new ServiceException("尝试识别直播平台失败");
        }
    }

    public DanMuService builder() throws IOException, ServiceException {
        //初始化保存模式
        DanMuExportService danMuExportService;
        switch (danMuExportType) {
            case JSON:
                danMuExportService = new JsonDanMuExportServiceImpl(saveName,danMuExportPattern);
            default: case SQLITE:
                danMuExportService = new SqliteDanMuExportServiceImpl(saveName, danMuExportPattern);break;
        }
        DanMuService danMuService;
        switch (websiteType) {
            case Huya:
                danMuService = new HuyaDanMuServiceImpl(liveUrl,danMuExportService,danMuClientEventManager);break;
            default:throw new ServiceException("未定义直播平台");
        }
        return danMuService;
    }

    public WebsiteType getWebsiteType() {
        return websiteType;
    }

    public void setWebsiteType(WebsiteType websiteType) {
        this.websiteType = websiteType;
    }

    public DanMuExportType getDanMuExportType() {
        return danMuExportType;
    }

    public void setDanMuExportType(DanMuExportType danMuExportType) {
        this.danMuExportType = danMuExportType;
    }

    public DanMuExportPattern getDanMuExportPattern() {
        return danMuExportPattern;
    }

    public void setDanMuExportPattern(DanMuExportPattern danMuExportPattern) {
        this.danMuExportPattern = danMuExportPattern;
    }

    public String getLiveUrl() {
        return liveUrl;
    }

    public void setLiveUrl(String liveUrl) {
        this.liveUrl = liveUrl;
    }

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public EventManager<DanMuClientEventType, DanMuClientEventResult> getDanMuClientEventManager() {
        return danMuClientEventManager;
    }

    public void setDanMuClientEventManager(EventManager<DanMuClientEventType, DanMuClientEventResult> danMuClientEventManager) {
        this.danMuClientEventManager = danMuClientEventManager;
    }
}