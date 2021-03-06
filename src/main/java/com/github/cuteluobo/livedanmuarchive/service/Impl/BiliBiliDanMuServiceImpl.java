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
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * BiliBili????????????
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
     * ???????????????????????????
     */
    private static final Pattern LIVE_ROOM_CODE_PATTERN = Pattern.compile("\\.\\S+/(\\S+)\\??");
    /** ?????????????????? **/
    private static final String LIVE_URL_PREFIX = "https://api.live.bilibili.com/room/v1/Room/room_init?id=";
    private static final String WS_URL = "wss://broadcastlv.chat.bilibili.com/sub";
//    private static final String WS_URL = "ws://broadcastlv.chat.bilibili.com:2244/sub/";
    /**????????????????????????*/
    public static final String SERVICE_MODEL_NAME = "huya";

    /**
     * ??????????????????URL
     */
    private String liveRoomUrl = null;
    /**
     * ?????????????????????url
     */
    private String interiorLiveRoomUrl = "";
    /**
     * ??????????????????URL??????/????????????????????????
     */
    private String liveRoomCode = "";

    /**
     * ?????????????????????
     */
    private String liveAnchorName;
    private LiveRoomData liveRoomData;


    private Map<String, String> useHeaders = new HashMap<>();
    private final int httpTimeOut = 60;
    private HttpRequest httpRequest;
    private HttpClient httpClient;
    private byte[] websocketCmdByteArray;

    /**
     * ?????????????????????
     */
    private DanMuExportService baseDanMuExportService;

    /**
     * ??????(??????)??????
     */
    private String saveName;

    /**
     * ??????????????????????????????null
     */
    private EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager;


    public BiliBiliDanMuServiceImpl(String liveRoomUrl, String saveName, DanMuExportService danMuExportService, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager) throws ServiceException {
        this.saveName = saveName;
        this.baseDanMuExportService = danMuExportService;
        try {
            heartbeatByteArray = Hex.decodeHex(heartbeat);
        } catch (DecoderException e) {
            logger.error("?????????????????????");
            e.printStackTrace();
        }
        if (liveRoomUrl == null || liveRoomUrl.trim().length() == 0) {
            logger.error("??????????????????url?????????{}", liveRoomUrl);
            throw new ServiceException("??????????????????url??????");
        }
        this.liveRoomUrl = liveRoomUrl;
        //?????????????????????
        Matcher matcher = LIVE_ROOM_CODE_PATTERN.matcher(liveRoomUrl);
        if (matcher.find()) {
            liveRoomCode = matcher.group(1);
        } else {
            logger.error("url??????????????????????????????{}", liveRoomUrl);
            throw new ServiceException("url???????????????????????????");
        }
        //?????????????????????
        interiorLiveRoomUrl = LIVE_URL_PREFIX + liveRoomCode;
        initHttpClient();
        this.eventManager = eventManager;
        //????????????????????????????????????????????????
        liveRoomData = new LiveRoomData();
        liveRoomData.setSaveName(saveName);
        liveRoomData.setLiveRoomCode(liveRoomCode);
        liveRoomData.setWebsiteType(serviceSupportWebsiteType);
        liveRoomData.setLiveRoomUrl(liveRoomUrl);
    }

    /**
     * ???????????????????????????httpclient
     * @throws URISyntaxException URI????????????
     */
    private void initHttpClient() throws ServiceException {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder();
        //??????????????????60s
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
            logger.error("?????????????????????????????????????????????????????????{}", interiorLiveRoomUrl);
            throw new ServiceException("???????????????????????????");
        }
    }

    /**
     * ?????????tar????????????
     * @throws IOException ??????IO?????????
     * @throws InterruptedException http??????????????????
     * @throws ServiceException ??????????????????
     */
    private Boolean initMessageParseRule() throws IOException, InterruptedException, ServiceException {
        //??????????????????
        HttpResponse<InputStream> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
        String body;
        //??????????????????gzip???????????????https://golb.hplar.ch/2019/01/java-11-http-client.html --- Compression??????
        String encoding = httpResponse.headers().firstValue("Content-Encoding").orElse("");
        if ("gzip".equals(encoding)) {
            logger.debug("??????????????????gzip??????");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (InputStream is = new GZIPInputStream(httpResponse.body()); var autoCloseOs = os) {
                is.transferTo(autoCloseOs);
            }
            body = os.toString(StandardCharsets.UTF_8);
        }else{
            logger.debug("?????????????????????");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (var is = httpResponse.body(); var autoCloseOs = os) {
                is.transferTo(autoCloseOs);
            }
            body = os.toString(StandardCharsets.UTF_8);
        }
        //?????????????????????????????????

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            //?????????????????????json??????
            JsonNode json = objectMapper.readTree(body);
            //???json??????????????????ID
            int roomId = json.get("data").get("room_id").intValue();
            int uid = json.get("data").get("uid").intValue();
            //?????????????????????json??????
            JsonFactory jsonFactory = objectMapper.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);
            jsonFactory = jsonFactory.setRootValueSeparator(",");

            ObjectNode objectNode = objectMapper.getNodeFactory().objectNode();
//            objectNode.put("uid", uid);
            objectNode.put("roomid", roomId);
            objectNode.put("uid", (long) (1e14 + 2e14 * new Random().nextDouble()));
            //1-???????????????,2-zlib???????????????3-brotli????????????(???????????????)
            objectNode.put("protover", 2);
            //?????????????????????
//            objectNode.put("type", 2);
            objectNode.put("platform", "web");
            //??????json
            String dataString = objectMapper.writeValueAsString(objectNode);
            logger.debug("goRoomString:{}",dataString);
            //???????????????
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            //???????????????
            dataOutputStream.writeInt(dataString.length()+16);
            dataOutputStream.write(Hex.decodeHex("00100001"));
            dataOutputStream.writeInt(7);
            dataOutputStream.writeInt(1);
            dataOutputStream.write(StringUtils.getBytesUsAscii(dataString));
            //??????????????????utf-8?????????????????????????????????????????????
//            dataOutputStream.writeUTF(dataString);
            websocketCmdByteArray = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            //??????????????????????????????
            logger.warn("{}???????????????????????????????????????????????????????????????????????????????????????",saveName);
            logger.debug("{}????????????????????????url???{},?????????????????????url???{}",saveName, liveRoomUrl, interiorLiveRoomUrl);
            logger.debug("????????????",e);
            if (eventManager != null) {
                DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
                danMuClientEventResult.setLiveRoomData(liveRoomData);
                danMuClientEventResult.setMessage("??????????????????????????????");
                danMuClientEventResult.setWebsocketConnectClose(true);
                eventManager.notify(DanMuClientEventType.CLOSE, danMuClientEventResult);
            }
            return false;
        }
        return true;
    }
    /**
     * ????????????Url
     *
     * @return ??????Url
     */
    @Override
    public String getLiveRoomUrl() {
        return liveRoomUrl;
    }

    /**
     * ??????????????????????????????
     *
     * @return ????????????????????????
     */
    @Override
    public WebsiteType getServiceSupportWebsiteType() {
        return serviceSupportWebsiteType;
    }

    /**
     * ?????????????????????
     *
     * @return ???????????????
     */
    @Override
    public String getLiveRoomCode() {
        return liveRoomCode;
    }

    /**
     * ????????????????????????
     *
     * @return ??????????????????
     */
    @Override
    public String getLiveAnchorName() {
        return liveAnchorName;
    }

    /**
     * ????????????--???????????????
     *
     * @param danMuExportService ?????????????????????
     * @throws InterruptedException ????????????
     * @throws ServiceException     ????????????
     * @throws IOException          URL/IO??????
     * @throws URISyntaxException   URI????????????
     */
    @Override
    public void startRecord(DanMuExportService danMuExportService) throws URISyntaxException, InterruptedException, ServiceException, IOException {
        WebSocketClient webSocketClient = null;
        try {
            if (initMessageParseRule()) {
                //TODO ??????????????????
                //JDK????????????WS?????????
//                BaseWebSocketClientByJdk baseWebSocketClientByJdk = new BaseWebSocketClientByJdk(new URI(WS_URL), useHeaders, 3600, HEARTBEAT_INTERVAL, heartbeatByteArray
//                        , new BiliBiliDanMuParseServiceImpl(danMuExportService), websocketCmdByteArray, eventManager, liveRoomData);
//                //?????????proxy
////                baseWebSocketClientByJdk.setProxy(new InetSocketAddress("127.0.0.1", 8888));
//                baseWebSocketClientByJdk.connect();
                //Tyrus??????WS??????????????????
//                BaseWebSocketClient2 webSocketClient2 = new BaseWebSocketClient2(new URI(WS_URL), useHeaders, 3600, HEARTBEAT_INTERVAL, heartbeatByteArray
//                        , new BiliBiliDanMuParseServiceImpl(danMuExportService), websocketCmdByteArray, eventManager, liveRoomData);
//                webSocketClient2.setProxy("http://127.0.0.1:8888");
//                webSocketClient2.connect();
                //???????????????
                webSocketClient = new BaseWebSocketClient(new URI(WS_URL), useHeaders, 3600, HEARTBEAT_INTERVAL, heartbeatByteArray
                        , new BiliBiliDanMuParseServiceImpl(danMuExportService), websocketCmdByteArray, eventManager, liveRoomData);
                //?????????proxy
//                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(8888));
//                webSocketClient.setProxy(proxy);
                webSocketClient.connect();
            }
        } catch (Exception e) {
            DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
            danMuClientEventResult.setLiveRoomData(liveRoomData);
            if (webSocketClient != null) {
                //???????????????ws??????
                webSocketClient.close();
                danMuClientEventResult.setWebsocketConnectClose(true);
            }
            danMuClientEventResult.setMessage("???????????????????????????");
            logger.error("??????: {},??????????????????????????????", saveName, e);
            if (eventManager != null) {
                eventManager.notify(DanMuClientEventType.CLOSE,danMuClientEventResult);
            }
        }
    }

    /**
     * ????????????
     *
     * @throws URISyntaxException   URI????????????
     * @throws InterruptedException ????????????
     * @throws ServiceException     ????????????
     * @throws IOException          URL/IO??????
     */
    @Override
    public void startRecord() throws URISyntaxException, InterruptedException, ServiceException, IOException {
        startRecord(baseDanMuExportService);
    }


}
