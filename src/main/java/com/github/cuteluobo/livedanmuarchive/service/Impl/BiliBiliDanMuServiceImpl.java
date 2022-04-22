package com.github.cuteluobo.livedanmuarchive.service.Impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuClientEventType;
import com.github.cuteluobo.livedanmuarchive.enums.WebsiteType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.listener.result.DanMuClientEventResult;
import com.github.cuteluobo.livedanmuarchive.manager.EventManager;
import com.github.cuteluobo.livedanmuarchive.pojo.LiveRoomData;
import com.github.cuteluobo.livedanmuarchive.service.DanMuExportService;
import com.github.cuteluobo.livedanmuarchive.service.DanMuService;
import com.github.cuteluobo.livedanmuarchive.websocketclient.BaseWebSocketClient;
import com.github.cuteluobo.livedanmuarchive.websocketclient.BaseWebSocketClient2;
import com.github.cuteluobo.livedanmuarchive.websocketclient.BaseWebSocketClientByJdk;
import com.google.gson.JsonObject;
import com.qq.tars.protocol.tars.TarsOutputStream;
import com.qq.tars.protocol.tars.annotation.TarsStruct;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.kafka.common.protocol.types.Struct;
import org.glassfish.tyrus.client.ClientManager;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * BiliBili弹幕服务
 * @author CuteLuoBo
 * @date 2022/4/17 15:18
 */
public class BiliBiliDanMuServiceImpl implements DanMuService {
    private final Logger logger = LoggerFactory.getLogger(BiliBiliDanMuServiceImpl.class);

//    private static String heartbeat = "0000001f001000010000000200000001";
    private static String heartbeat = "0000001f0010000100000002000000015b6f626a656374204f626a6563745d";
    private static byte[] heartbeatByteArray;
    private static final int HEARTBEAT_INTERVAL = 30;

    private final WebsiteType serviceSupportWebsiteType = WebsiteType.Bil;
    /**
     * 直播间代号匹配正则
     */
    private static final Pattern LIVE_ROOM_CODE_PATTERN = Pattern.compile("\\.\\S+/(\\S+)\\??");
    /** 默认网址前缀 **/
    private static final String LIVE_URL_PREFIX = "https://api.live.bilibili.com/room/v1/Room/room_init?id=";
    private static final String WS_URL = "wss://broadcastlv.chat.bilibili.com/sub";
//    private static final String WS_URL = "ws://broadcastlv.chat.bilibili.com:2244/sub/";
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
    private EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager;


    public BiliBiliDanMuServiceImpl(String liveRoomUrl, String saveName, DanMuExportService danMuExportService, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager) throws ServiceException {
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
            body = os.toString(StandardCharsets.UTF_8);
        }else{
            logger.debug("请求页面未压缩");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (var is = httpResponse.body(); var autoCloseOs = os) {
                is.transferTo(autoCloseOs);
            }
            body = os.toString(StandardCharsets.UTF_8);
        }
        //获取返回网页信息字符串

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            //将返回结果转为json读取
            JsonNode json = objectMapper.readTree(body);
            //从json中获取直播间ID
            int roomId = json.get("data").get("room_id").intValue();
            int uid = json.get("data").get("uid").intValue();
            //构建传输数据的json对象
            JsonFactory jsonFactory = objectMapper.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);
            jsonFactory = jsonFactory.setRootValueSeparator(",");

            ObjectNode objectNode = objectMapper.getNodeFactory().objectNode();
//            objectNode.put("uid", uid);
            objectNode.put("roomid", roomId);
            objectNode.put("uid", (long) (1e14 + 2e14 * new Random().nextDouble()));
            //1-未压缩消息,2-zlib压缩消息，3-brotli压缩消息(暂无法解析)
            objectNode.put("protover", 2);
            //可能跟屏蔽有关
//            objectNode.put("type", 2);
            objectNode.put("platform", "web");
            //构建json
            String dataString = objectMapper.writeValueAsString(objectNode);
            logger.debug("goRoomString:{}",dataString);
            //构建结构体
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            //握手包长度
            dataOutputStream.writeInt(dataString.length()+16);
            dataOutputStream.write(Hex.decodeHex("00100001"));
            dataOutputStream.writeInt(7);
            dataOutputStream.writeInt(1);
            dataOutputStream.write(StringUtils.getBytesUsAscii(dataString));
            //错误方法，用utf-8写入会带入额外标识导致无法识别
//            dataOutputStream.writeUTF(dataString);
            websocketCmdByteArray = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            //由监听器进行定时重试
            logger.warn("{}任务，直播间弹幕源获取失败，可能直播未开播，稍后将进行重试",saveName);
            logger.debug("{}任务，传入的直播url：{},用于获取信息的url：{}",saveName, liveRoomUrl, interiorLiveRoomUrl);
            logger.debug("堆栈错误",e);
            if (eventManager != null) {
                DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
                danMuClientEventResult.setLiveRoomData(liveRoomData);
                danMuClientEventResult.setMessage("直播间弹幕源获取异常");
                eventManager.notify(DanMuClientEventType.ERROR, danMuClientEventResult);
            }
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

    /**
     * 开始录制--自定义接口
     *
     * @param danMuExportService 使用的导出接口
     * @throws InterruptedException 线程错误
     * @throws ServiceException     服务错误
     * @throws IOException          URL/IO错误
     * @throws URISyntaxException   URI解析错误
     */
    @Override
    public void startRecord(DanMuExportService danMuExportService) throws URISyntaxException, InterruptedException, ServiceException, IOException {
        try {
            if (initMessageParseRule()) {
                //TODO
                BaseWebSocketClientByJdk baseWebSocketClientByJdk = new BaseWebSocketClientByJdk(new URI(WS_URL), useHeaders, 3600, HEARTBEAT_INTERVAL, heartbeatByteArray
                        , new BiliBiliDanMuParseServiceImpl(danMuExportService), websocketCmdByteArray, eventManager, liveRoomData);
                //TODO 调试用proxy
                baseWebSocketClientByJdk.setProxy(new InetSocketAddress("127.0.0.1", 8888));
                baseWebSocketClientByJdk.connect();
//                BaseWebSocketClient2 webSocketClient2 = new BaseWebSocketClient2(new URI(WS_URL), useHeaders, 3600, HEARTBEAT_INTERVAL, heartbeatByteArray
//                        , new BiliBiliDanMuParseServiceImpl(danMuExportService), websocketCmdByteArray, eventManager, liveRoomData);
//                webSocketClient2.setProxy("http://127.0.0.1:8888");
//                webSocketClient2.connect();
                //旧实现方法
//                WebSocketClient webSocketClient = new BaseWebSocketClient(new URI(WS_URL), useHeaders, 3600, HEARTBEAT_INTERVAL, heartbeatByteArray
//                        , new BiliBiliDanMuParseServiceImpl(danMuExportService), websocketCmdByteArray, eventManager, liveRoomData);
//                //TODO 调试用proxy
//                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(8888));
//                webSocketClient.setProxy(proxy);
//                webSocketClient.connect();
            }
        } catch (Exception e) {
            DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
            danMuClientEventResult.setLiveRoomData(liveRoomData);
            danMuClientEventResult.setMessage("录制启动时出现错误");
            logger.error("任务: {},启动录制时出现错误：", saveName, e);
            if (eventManager != null) {
                eventManager.notify(DanMuClientEventType.ERROR,danMuClientEventResult);
            }
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
