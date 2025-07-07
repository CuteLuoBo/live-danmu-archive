package com.github.cuteluobo.livedanmuarchive.service.Impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuClientEventType;
import com.github.cuteluobo.livedanmuarchive.enums.WebsiteType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.listener.result.DanMuClientEventResult;
import com.github.cuteluobo.livedanmuarchive.manager.EventManager;
import com.github.cuteluobo.livedanmuarchive.pojo.LiveRoomData;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseUserInfo;
import com.github.cuteluobo.livedanmuarchive.service.AbstractBaseDanMuService;
import com.github.cuteluobo.livedanmuarchive.service.DanMuExportService;
import com.github.cuteluobo.livedanmuarchive.service.DanMuService;
import com.github.cuteluobo.livedanmuarchive.service.Impl.parse.BiliBiliDanMuParseServiceImpl;
import com.github.cuteluobo.livedanmuarchive.utils.BiliInfoUtil;
import com.github.cuteluobo.livedanmuarchive.utils.LinkUtil;
import com.github.cuteluobo.livedanmuarchive.websocketclient.BaseWebSocketClient;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
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
public class BiliBiliDanMuServiceImpl extends AbstractBaseDanMuService {
    private final Logger logger = LoggerFactory.getLogger(BiliBiliDanMuServiceImpl.class);

    private static final String heartbeat = "0000001f0010000100000002000000015b6f626a656374204f626a6563745d";
    private static byte[] heartbeatByteArray;
    private static final int HEARTBEAT_INTERVAL = 30;

    /**
     * 直播认证消息
     */
    private BaseUserInfo baseUserInfo;

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
    public static final String SERVICE_MODEL_NAME = "bili";

    /**
     * 传入的直播间URL
     */
    private String liveRoomUrl;
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

    private byte[] websocketCmdByteArray = new byte[0];

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


    public BiliBiliDanMuServiceImpl(String liveRoomUrl, String saveName, DanMuExportService danMuExportService, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager,BaseUserInfo baseUserInfo) throws ServiceException, URISyntaxException {
        super(WebsiteType.Bil, heartbeat, liveRoomUrl, saveName, danMuExportService, eventManager);
        this.saveName = saveName;
        this.baseDanMuExportService = danMuExportService;
        try {
            heartbeatByteArray = Hex.decodeHex(heartbeat);
        } catch (DecoderException e) {
            logger.error("心跳包转码错误",e);
        }
        if (liveRoomUrl == null || liveRoomUrl.trim().isEmpty()) {
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
        this.eventManager = eventManager;
        //储存直播间信息，用于后续监听调用
        liveRoomData = new LiveRoomData();
        liveRoomData.setSaveName(saveName);
        liveRoomData.setLiveRoomCode(liveRoomCode);
        liveRoomData.setWebsiteType(serviceSupportWebsiteType);
        liveRoomData.setLiveRoomUrl(liveRoomUrl);
        this.baseUserInfo = baseUserInfo;
        initMessageParseRule();
    }


    /**
     * 交由子类实现的创建接口体方法
     *
     * @return 连接与解析指令
     * @throws Exception
     */
    @Override
    protected List<byte[]> createWebsocketCmdByteList() throws Exception {
        //获取返回数据
        String body = LinkUtil.getHtmlBody(interiorLiveRoomUrl);;
        //获取返回网页信息字符串
        ObjectMapper objectMapper = new ObjectMapper();
        //将返回结果转为json读取
        JsonNode json = objectMapper.readTree(body);
        //从json中获取直播间ID
        int roomId = json.get("data").get("room_id").intValue();
        int uid = json.get("data").get("uid").intValue();
        //构建传输数据的json对象
        ObjectNode objectNode = objectMapper.getNodeFactory().objectNode();
        //使用当前访问用户的UID
            objectNode.put("uid", baseUserInfo.getUid());
//            objectNode.put("uid", 0);
        objectNode.put("roomid", roomId);
        //填入随机用户UID（失效）
//            objectNode.put("uid", (long) (1e14 + 2e14 * new Random().nextDouble()));
        //1-未压缩消息,2-zlib压缩消息，3-brotli压缩消息(暂无法解析)
        objectNode.put("protover", 2);
        //可能跟屏蔽有关
//            objectNode.put("type", 2);
        objectNode.put("platform", "web");
        objectNode.put("key", BiliInfoUtil.getAuthInfo(roomId, baseUserInfo).getToken());
        //构建json
        String dataString = objectMapper.writeValueAsString(objectNode);
        logger.debug("goRoomString:{}",dataString);
        //构建结构体
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        //握手包长度(正文+头部)
        dataOutputStream.writeInt(dataString.length()+16);
        //头部长度
        dataOutputStream.writeShort(16);
        //协议版本(1: 心跳及认证包 (正文不使用压缩))
        dataOutputStream.writeShort(1);
//            dataOutputStream.write(Hex.decodeHex("00100001"));
        //操作码 (7-	认证包)
        dataOutputStream.writeInt(7);
        //sequence, 每次发包时向上递增
        dataOutputStream.writeInt(1);
        //正文
        dataOutputStream.write(StringUtils.getBytesUsAscii(dataString));
        websocketCmdByteArray = byteArrayOutputStream.toByteArray();
        return List.of(websocketCmdByteArray);
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
        WebSocketClient webSocketClient = null;
        try {
                //JDK原生实现WS客户端
//                BaseWebSocketClientByJdk baseWebSocketClientByJdk = new BaseWebSocketClientByJdk(new URI(WS_URL), useHeaders, 3600, HEARTBEAT_INTERVAL, heartbeatByteArray
//                        , new BiliBiliDanMuParseServiceImpl(danMuExportService), websocketCmdByteArray, eventManager, liveRoomData);
//                //调试用proxy
////                baseWebSocketClientByJdk.setProxy(new InetSocketAddress("127.0.0.1", 8888));
//                baseWebSocketClientByJdk.connect();
                //Tyrus实现WS客户端，弃用
//                BaseWebSocketClient2 webSocketClient2 = new BaseWebSocketClient2(new URI(WS_URL), useHeaders, 3600, HEARTBEAT_INTERVAL, heartbeatByteArray
//                        , new BiliBiliDanMuParseServiceImpl(danMuExportService), websocketCmdByteArray, eventManager, liveRoomData);
//                webSocketClient2.setProxy("http://127.0.0.1:8888");
//                webSocketClient2.connect();
                //旧实现方法

            webSocketClient = new BaseWebSocketClient(new URI(WS_URL), useHeaders, 3600, HEARTBEAT_INTERVAL, heartbeatByteArray
                    , new BiliBiliDanMuParseServiceImpl(danMuExportService), List.of(websocketCmdByteArray), eventManager, liveRoomData);
            //调试用proxy
//                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(8888));
//                webSocketClient.setProxy(proxy);
            webSocketClient.connect();
        } catch (Exception e) {
            DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
            danMuClientEventResult.setLiveRoomData(liveRoomData);
            if (webSocketClient != null) {
                //关闭并废弃ws连接
                webSocketClient.close();
                danMuClientEventResult.setWebsocketConnectClose(true);
            }
            danMuClientEventResult.setMessage("录制启动时出现错误");
            logger.error("任务: {},启动录制时出现错误：", saveName, e);
            if (eventManager != null) {
                eventManager.notify(DanMuClientEventType.CLOSE,danMuClientEventResult);
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
