package com.github.cuteluobo.livedanmuarchive.service.Impl;

import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
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
import java.nio.ByteBuffer;
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
    //    private static byte[] heartbeatByteArray = ByteUtil.parseHexString(heartbeat);转换错误
    private static byte[] heartbeatByteArray;

    /** 解析信息的匹配规则 **/
//    private static final Pattern TT_META_DATA = Pattern.compile("TT_META_DATA(\\s)+}", Pattern.MULTILINE);
    private static final Pattern YYID =Pattern.compile("lYyid\":([0-9]+)", Pattern.MULTILINE);
    private static final Pattern TID =Pattern.compile("lChannelId\":([0-9]+)", Pattern.MULTILINE);
    private static final Pattern SID  =Pattern.compile("lSubChannelId\":([0-9]+)", Pattern.MULTILINE);
    /** 默认网址前缀 **/
    private static final String LIVE_URL_PREFIX = "https://m.huya.com/";
    private static final String WS_URL = "wss://cdnws.api.huya.com/";

    private String liveRoomString = null;
    private String liveRoomUrl = "";
    private Map<String, String> useHeaders = new HashMap<>();
    private int httpTimeOut = 60;
    private HttpRequest httpRequest;
    private HttpClient httpClient;
    private byte[] websocketCmdByteArray;


    public HuyaDanMuServiceImpl(String liveRoomString) throws ServiceException, IOException, InterruptedException {
        try {
            heartbeatByteArray = Hex.decodeHex(heartbeat);
        } catch (DecoderException e) {
            logger.error("心跳包转码错误");
            e.printStackTrace();
        }
        if (liveRoomString == null || liveRoomString.trim().length() == 0) {
            logger.error("填入的直播间号无效");
            throw new ServiceException("填入的直播间号无效");
        }
        this.liveRoomString = liveRoomString;
        //拼接直播间连接
        liveRoomUrl = LIVE_URL_PREFIX + liveRoomString;
        initHttpClient();
        initMessageParseRule();
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
            URI uri = new URI(liveRoomUrl);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri).GET();
            for (Map.Entry<String, String> entry :
                    useHeaders.entrySet()) {
                requestBuilder.setHeader(entry.getKey(), entry.getValue());
            }
            httpRequest = requestBuilder.build();
            httpClient = clientBuilder.build();
        } catch (URISyntaxException uriSyntaxException) {
            logger.error("解析直播间网址失败，尝试解析的字符串：{}",liveRoomUrl);
            throw new ServiceException("解析直播间网址失败");
        }
    }

    private void initMessageParseRule() throws IOException, InterruptedException, ServiceException {
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
            body = new String(os.toByteArray(), StandardCharsets.UTF_8);
        }else{
            logger.debug("请求页面未压缩");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (var is = httpResponse.body(); var autoCloseOs = os) {
                is.transferTo(autoCloseOs);
            }
            body = new String(os.toByteArray(), StandardCharsets.UTF_8);
        }
        //获取返回网页信息字符串
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
            //TODO 当主播未开播时此值会缺失，后续增加提示
            long ayyuid = Long.parseLong(yyidMatcher.group(1));
            Matcher tidMatcher = TID.matcher(body);
            tidMatcher.find();
            long tid = Long.parseLong(tidMatcher.group(1));
            Matcher sidMatcher = SID.matcher(body);
            sidMatcher.find();
            long sid = Long.parseLong(sidMatcher.group(1));

            //主播名称"sNick":"***"


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
            logger.error("房间信息解析错误");
            throw illegalStateException;
        }

    }

    /**
     * 开始记录
     * @param danMuExportService 使用的导出接口
     * @throws URISyntaxException URI解析失败错误
     */
    @Override
    public void startRecord(DanMuExportService danMuExportService) throws URISyntaxException, InterruptedException {
        WebSocketClient webSocketClient = new BaseWebSocketClient(new URI(WS_URL), useHeaders, 3600, 60, heartbeatByteArray
                , new HuyaDanMuParseServiceImpl(danMuExportService),websocketCmdByteArray);
        webSocketClient.connect();
    }
}
