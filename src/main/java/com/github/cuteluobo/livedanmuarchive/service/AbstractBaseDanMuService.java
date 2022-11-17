package com.github.cuteluobo.livedanmuarchive.service;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuClientEventType;
import com.github.cuteluobo.livedanmuarchive.enums.WebsiteType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.listener.result.DanMuClientEventResult;
import com.github.cuteluobo.livedanmuarchive.manager.EventManager;
import com.github.cuteluobo.livedanmuarchive.pojo.LiveRoomData;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CuteLuoBo
 * @date 2022/11/16 17:23
 */
public abstract class AbstractBaseDanMuService implements DanMuService {
    Logger logger = LoggerFactory.getLogger(AbstractBaseDanMuService.class);
    private static byte[] heartbeatByteArray;
    private static final int HEARTBEAT_INTERVAL = 60;

    private final WebsiteType serviceSupportWebsiteType;
    /**
     * 直播间代号匹配正则
     */
    private static final Pattern LIVE_ROOM_CODE_PATTERN = Pattern.compile("\\.\\S+/(\\S+)\\??");
    /** 默认网址前缀 **/

    /**
     * 传入的直播间URL
     */
    private String liveRoomUrl = null;
    /**
     * 直播间代号即URL最后/部分数字或者字母
     */
    private String liveRoomCode = "";

    /**
     * 直播间主播名称
     */
    private String liveAnchorName;
    private LiveRoomData liveRoomData;


    private final int httpTimeOut = 60;
    private List<byte[]> websocketCmdByteList;

    /**
     * 默认的导出服务
     */
    private DanMuExportService baseDanMuExportService;

    /**
     * 保存(任务)名称
     */
    private String saveName;

    /**
     * 监听事件管理器，可为null
     */
    private EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager;

    public AbstractBaseDanMuService(WebsiteType serviceSupportWebsiteType, String heartbeat, String liveRoomUrl, String saveName, DanMuExportService danMuExportService, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager) throws ServiceException, URISyntaxException {

        this.serviceSupportWebsiteType = serviceSupportWebsiteType;
        this.saveName = saveName;
        this.baseDanMuExportService = danMuExportService;
        try {
            heartbeatByteArray = Hex.decodeHex(heartbeat);
        } catch (DecoderException e) {
            logger.error("心跳包转码错误");
            e.printStackTrace();
        }
        if (liveRoomUrl == null || liveRoomUrl.trim().length() == 0) {
            logger.error("传入的直播间url无效：{}", liveRoomUrl);
            throw new ServiceException("传入的直播间url无效");
        }
        this.liveRoomUrl = liveRoomUrl;
        //解析直播间代号
        Matcher matcher = LIVE_ROOM_CODE_PATTERN.matcher(liveRoomUrl);
        if (matcher.find()) {
            liveRoomCode = matcher.group(1);
        } else {
            logger.error("url未解析出直播间代号：{}", liveRoomUrl);
            throw new ServiceException("url未解析出直播间代号");
        }
        this.eventManager = eventManager;
        //储存直播间信息，用于后续监听调用
        liveRoomData = new LiveRoomData();
        liveRoomData.setSaveName(saveName);
        liveRoomData.setLiveRoomCode(liveRoomCode);
        liveRoomData.setWebsiteType(this.serviceSupportWebsiteType);
        liveRoomData.setLiveRoomUrl(liveRoomUrl);
    }

    /**
     * 初始化tar解析规则
     * @throws IOException 网页IO流错误
     * @throws InterruptedException http请求中断错误
     * @throws ServiceException 服务运行错误
     */
    protected boolean initMessageParseRule() throws IOException, InterruptedException, URISyntaxException {
        try {
            websocketCmdByteList = createWebsocketCmdByteList();
        } catch (Exception e) {
            //由监听器进行定时重试
            logger.warn("{}任务，直播间弹幕源获取失败，可能直播未开播",saveName);
            logger.debug("{}任务，传入的直播url：{}",saveName, liveRoomUrl);
            logger.debug("堆栈错误",e);
            if (eventManager != null) {
                logger.info("{}任务，稍后将进行重启尝试",saveName);
                DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
                danMuClientEventResult.setLiveRoomData(liveRoomData);
                danMuClientEventResult.setMessage("直播间弹幕源获取异常");
                danMuClientEventResult.setWebsocketConnectClose(true);
                eventManager.notify(DanMuClientEventType.CLOSE, danMuClientEventResult);
            } else {
                logger.warn("{}任务，未设置重试监听管理，略过重启",saveName);
            }
            return false;
        }
        return true;
    }

    /**
     * 交由子类实现的创建接口体方法
     * @return 连接与解析指令
     * @throws Exception
     */
    protected abstract List<byte[]> createWebsocketCmdByteList() throws Exception;

    /**
     * 开始录制
     *
     * @throws URISyntaxException   URI解析错误
     * @throws InterruptedException 线程错误
     * @throws ServiceException     服务错误
     * @throws IOException          URL/IO错误
     */
    @Override
    public void startRecord() throws URISyntaxException, InterruptedException, ServiceException, IOException {
        startRecord(baseDanMuExportService);
    }

    /**
     * 获取直播Url
     *
     * @return 直播Url
     */
    @Override
    public String getLiveRoomUrl() {
        return liveRoomUrl;
    }

    /**
     * 获取服务支持平台类型
     *
     * @return 服务支持平台类型
     */
    @Override
    public WebsiteType getServiceSupportWebsiteType() {
        return serviceSupportWebsiteType;
    }

    /**
     * 获取直播间代号
     *
     * @return 直播间代号
     */
    @Override
    public String getLiveRoomCode() {
        return liveRoomCode;
    }

    /**
     * 获取直播主播名称
     *
     * @return 直播主播名称
     */
    @Override
    public String getLiveAnchorName() {
        return liveAnchorName;
    }

    public byte[] getHeartbeatByteArray() {
        return heartbeatByteArray;
    }

    public void setHeartbeatByteArray(byte[] heartbeatByteArray) {
        AbstractBaseDanMuService.heartbeatByteArray = heartbeatByteArray;
    }

    public List<byte[]> getWebsocketCmdByteList() {
        return websocketCmdByteList;
    }

    public void setWebsocketCmdByteList(List<byte[]> websocketCmdByteList) {
        this.websocketCmdByteList = websocketCmdByteList;
    }

    public DanMuExportService getBaseDanMuExportService() {
        return baseDanMuExportService;
    }

    public void setBaseDanMuExportService(DanMuExportService baseDanMuExportService) {
        this.baseDanMuExportService = baseDanMuExportService;
    }

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public EventManager<DanMuClientEventType, DanMuClientEventResult> getEventManager() {
        return eventManager;
    }

    public void setEventManager(EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager) {
        this.eventManager = eventManager;
    }

    public LiveRoomData getLiveRoomData() {
        return liveRoomData;
    }

    public void setLiveRoomData(LiveRoomData liveRoomData) {
        this.liveRoomData = liveRoomData;
    }

    public void setLiveRoomUrl(String liveRoomUrl) {
        this.liveRoomUrl = liveRoomUrl;
    }

    public void setLiveRoomCode(String liveRoomCode) {
        this.liveRoomCode = liveRoomCode;
    }

    public void setLiveAnchorName(String liveAnchorName) {
        this.liveAnchorName = liveAnchorName;
    }
}
