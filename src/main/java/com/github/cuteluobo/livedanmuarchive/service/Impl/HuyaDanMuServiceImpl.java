package com.github.cuteluobo.livedanmuarchive.service.Impl;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuClientEventType;
import com.github.cuteluobo.livedanmuarchive.enums.WebsiteType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.listener.result.DanMuClientEventResult;
import com.github.cuteluobo.livedanmuarchive.manager.EventManager;
import com.github.cuteluobo.livedanmuarchive.pojo.LiveRoomData;
import com.github.cuteluobo.livedanmuarchive.service.DanMuExportService;
import com.github.cuteluobo.livedanmuarchive.service.DanMuService;
import com.github.cuteluobo.livedanmuarchive.websocketclient.BaseWebSocketClient;
import com.qq.tars.protocol.tars.TarsOutputStream;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * 虎牙弹幕获取实现类
 * @author CuteLuoBo
 * @date 2021/12/16 16:20
 */
public class HuyaDanMuServiceImpl implements DanMuService {
    private final Logger logger = LoggerFactory.getLogger(HuyaDanMuServiceImpl.class);

    private static String heartbeat = "00031d0000690000006910032c3c4c56086f6e6c696e657569660f4f6e557365724865617274426561747d00003c0800010604745265711d00002f0a0a0c1600260036076164725f77617046000b1203aef00f2203aef00f3c426d5202605c60017c82000bb01f9cac0b8c980ca80c";
    private static byte[] heartbeatByteArray;

    private final WebsiteType serviceSupportWebsiteType = WebsiteType.Huya;




    //    private static byte[] heartbeatByteArray = ByteUtil.parseHexString(heartbeat);转换错误



//    private static final Pattern TT_META_DATA = Pattern.compile("TT_META_DATA(\\s)+}", Pattern.MULTILINE);
    /** 解析信息的匹配规则 **/
    private static final Pattern YYID =Pattern.compile("lYyid\":([0-9]+)\"", Pattern.MULTILINE);
    private static final Pattern TID =Pattern.compile("lChannelId\":([0-9]+)\"", Pattern.MULTILINE);
    private static final Pattern SID  =Pattern.compile("lSubChannelId\":([0-9]+)\"", Pattern.MULTILINE);
    private static final Pattern NICK = Pattern.compile("sNick\":(\\S+)\"", Pattern.MULTILINE);
    /**
     * 直播间代号匹配正则
     */
    private static final Pattern LIVE_ROOM_CODE_PATTERN = Pattern.compile("\\.\\S+/(\\S+)\\??");
    /** 默认网址前缀 **/
    private static final String LIVE_URL_PREFIX = "https://m.huya.com/";
    private static final String WS_URL = "wss://wsapi.huya.com/";
    private static final String WS_CDN_URL = "wss://cdnws.api.huya.com/";
    /**服务运行标志名称*/
    public static final String SERVICE_MODEL_NAME = "huya";

    /**
     * 传入的直播间URL
     */
    private String liveRoomUrl = null;
    /**
     * 内部拼接使用的url
     */
    private String interiorLiveRoomUrl = "";
    /**
     * 直播间代号即URL最后/部分数字或者字母
     */
    private String liveRoomCode = "";

    /**
     * 直播间主播名称
     */
    private String liveAnchorName;
    private LiveRoomData liveRoomData;


    private Map<String, String> useHeaders = new HashMap<>();
    private final int httpTimeOut = 60;
    private HttpRequest httpRequest;
    private HttpClient httpClient;
    private byte[] websocketCmdByteArray;

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
    private EventManager<DanMuClientEventType,DanMuClientEventResult> eventManager;


    public HuyaDanMuServiceImpl(String liveRoomUrl, String saveName, DanMuExportService danMuExportService, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager) throws ServiceException {
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
        //拼接直播间连接
        interiorLiveRoomUrl = LIVE_URL_PREFIX + liveRoomCode;
        initHttpClient();
        this.eventManager = eventManager;
        //储存直播间信息，用于后续监听调用
        liveRoomData = new LiveRoomData();
        liveRoomData.setSaveName(saveName);
        liveRoomData.setLiveRoomCode(liveRoomCode);
        liveRoomData.setWebsiteType(serviceSupportWebsiteType);
        liveRoomData.setLiveRoomUrl(liveRoomUrl);
    }

    /**
     * 初始化连接直播间的httpclient
     * @throws URISyntaxException URI解析错误
     */
    private void initHttpClient() throws ServiceException {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder();
        //连接超时时间60s
        clientBuilder.connectTimeout(Duration.ofSeconds(60));
        useHeaders.put("user-agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML," +
                "like Gecko) Chrome/79.0.3945.88 Mobile Safari/537.36");
        try {
            URI uri = new URI(interiorLiveRoomUrl);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri).GET();
            for (Map.Entry<String, String> entry :
                    useHeaders.entrySet()) {
                requestBuilder.setHeader(entry.getKey(), entry.getValue());
            }
            httpRequest = requestBuilder.build();
            httpClient = clientBuilder.build();
        } catch (URISyntaxException uriSyntaxException) {
            logger.error("解析直播间网址失败，尝试解析的字符串：{}", interiorLiveRoomUrl);
            throw new ServiceException("解析直播间网址失败");
        }
    }

    /**
     * 初始化tar解析规则
     * @throws IOException 网页IO流错误
     * @throws InterruptedException http请求中断错误
     * @throws ServiceException 服务运行错误
     */
    private Boolean initMessageParseRule() throws IOException, InterruptedException, ServiceException {
        //获取返回数据
        HttpResponse<InputStream> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
        String body;
        //验证是否存在gzip压缩，参考https://golb.hplar.ch/2019/01/java-11-http-client.html --- Compression章节
        String encoding = httpResponse.headers().firstValue("Content-Encoding").orElse("");
        if ("gzip".equals(encoding)) {
            logger.debug("请求页面使用gzip压缩");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (InputStream is = new GZIPInputStream(httpResponse.body()); var autoCloseOs = os) {
                is.transferTo(autoCloseOs);
            }
//            body = new String(os.toByteArray(), StandardCharsets.UTF_8);
            body = os.toString(StandardCharsets.UTF_8);
        }else{
            logger.debug("请求页面未压缩");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (var is = httpResponse.body(); var autoCloseOs = os) {
                is.transferTo(autoCloseOs);
            }
//            body = new String(os.toByteArray(), StandardCharsets.UTF_8);
            body = os.toString(StandardCharsets.UTF_8);
        }
        //获取返回网页信息字符串
        //主播名称"sNick":"***"
        Matcher liveAnchorMatcher = NICK.matcher(body);
        if (liveAnchorMatcher.find()) {
            liveAnchorName = liveAnchorMatcher.group(1);
            liveRoomData.setLiveAnchorName(liveAnchorName);
        }

        try {
            //截取网页js中TT_META_DATA部分，废弃，解析失败
//            Matcher roomDataMatcher = TT_META_DATA.matcher(body);
//            String roomData = roomDataMatcher.group(1);
//            if (roomData == null || roomData.trim().length() == 0) {
//                throw new ServiceException(liveRoomString + "直播间，RoomData解析失败");
//            }

            //获取各tar解析参数
            Matcher yyidMatcher = YYID.matcher(body);
            yyidMatcher.find();
            Matcher tidMatcher = TID.matcher(body);
            tidMatcher.find();
            Matcher sidMatcher = SID.matcher(body);
            sidMatcher.find();

            String ayyuidString;
            String tidString;
            String sidString;



            try {
                ayyuidString = yyidMatcher.group(1);
                tidString = tidMatcher.group(1);
                sidString = sidMatcher.group(1);
                logger.debug("ayyuid:{},tid:{},sid:{}",ayyuidString,tidString,sidString);
            } catch (IllegalStateException illegalStateException) {
                //由监听器进行定时重试
                logger.warn("{}任务，直播间弹幕源获取失败，可能直播未开播，稍后将进行重试",saveName);
                logger.debug("{}任务，传入的直播url：{},用于获取信息的url：{}",saveName, liveRoomUrl, interiorLiveRoomUrl);
                DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
                danMuClientEventResult.setLiveRoomData(liveRoomData);
                danMuClientEventResult.setMessage("直播间弹幕源获取异常");
                eventManager.notify(DanMuClientEventType.ERROR,danMuClientEventResult);
                return false;
            }

            long ayyuid = Long.parseLong(ayyuidString);
            long tid = Long.parseLong(tidString);
            long sid = Long.parseLong(sidString);




            //Tars解析相关 参考https://github.com/wbt5/real-url/blob/master/danmu/danmaku/huya.py
            TarsOutputStream tarsOutputStream = new TarsOutputStream();
            //写入解析模式
            tarsOutputStream.write(ayyuid, 0);
            tarsOutputStream.write(Boolean.TRUE, 1);
            tarsOutputStream.write("", 2);
            tarsOutputStream.write("", 3);
            tarsOutputStream.write(tid, 4);
            tarsOutputStream.write(sid, 5);
            tarsOutputStream.write(0, 6);
            tarsOutputStream.write(0, 7);

            //ws解析指令
            TarsOutputStream websocketCmd = new TarsOutputStream();
            websocketCmd.write(1, 0);
            websocketCmd.write(tarsOutputStream.toByteArray(), 1);
            websocketCmdByteArray = websocketCmd.toByteArray();


        } catch (IllegalStateException illegalStateException) {
            //由监听器进行定时重试
            logger.warn("{}直播间信息解析错误，稍后将进行重试，传入的直播url：{},用于获取信息的url：{}", liveRoomUrl, interiorLiveRoomUrl);
            DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
            danMuClientEventResult.setLiveRoomData(liveRoomData);
            danMuClientEventResult.setMessage("直播间信息解析错误");
            eventManager.notify(DanMuClientEventType.ERROR,danMuClientEventResult);
            logger.debug("堆栈信息",illegalStateException);
            return false;
        }
        return true;
    }


    /**
     * 获取直播Url
     *
     * @return 直播Url
     */
    @Override
    public String getLiveRoomUrl() {
        return null;
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

    /**
     * 开始记录--自定义导出接口
     * @param danMuExportService 使用的导出接口
     * @throws InterruptedException 线程错误
     * @throws ServiceException     服务错误
     * @throws IOException          URL/IO错误
     * @throws URISyntaxException URI解析错误
     */
    @Override
    public void startRecord(DanMuExportService danMuExportService) throws URISyntaxException, InterruptedException, ServiceException, IOException {
        try {
            if (initMessageParseRule()) {
                WebSocketClient webSocketClient = new BaseWebSocketClient(new URI(WS_CDN_URL), useHeaders, 3600, 60, heartbeatByteArray
                        , new HuyaDanMuParseServiceImpl(danMuExportService), websocketCmdByteArray, eventManager, liveRoomData);
                webSocketClient.connect();
            }
        } catch (Exception e) {
            DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
            danMuClientEventResult.setLiveRoomData(liveRoomData);
            danMuClientEventResult.setMessage("录制启动时出现错误");
            logger.error("任务: {},启动录制时出现错误：", saveName, e);
            eventManager.notify(DanMuClientEventType.ERROR,danMuClientEventResult);
        }

    }

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
}
