package com.github.cuteluobo.livedanmuarchive.websocketclient;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuClientEventType;
import com.github.cuteluobo.livedanmuarchive.listener.result.DanMuClientEventResult;
import com.github.cuteluobo.livedanmuarchive.manager.EventManager;
import com.github.cuteluobo.livedanmuarchive.pojo.LiveRoomData;
import com.github.cuteluobo.livedanmuarchive.service.DanMuParseService;
import com.github.cuteluobo.livedanmuarchive.utils.WebSocketInterval;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

/**
 * jdk11+原生实现的WS客户端
 * @author CuteLuoBo
 * @date 2022/4/20 21:02
 */
public class BaseWebSocketClientByJdk implements IntervalRun {
    Logger logger = LoggerFactory.getLogger(getClass());
    private WebSocketInterval webSocketInterval;
    private String intervalSendStringMessage = null;
    private byte[] intervalSendStringByteArray = null;
    private URI serverUri;
    private int intervalSecond = 60;
    private ScheduledExecutorService scheduledExecutorService;
    private int connectTimeout = 3600;
    private Map<String, String> httpHeaders;

    private HttpClient httpClient;
    private WebSocket.Builder wsBuilder;
    private CompletableFuture<WebSocket> webSocketCompletableFuture;
    private WebSocket webSocket;
    private HttpClient.Builder httpClientBuilder;


    private byte[] handshakeDataByteArray;


    /**
     * 弹幕解析实现类
     */
    private DanMuParseService danMuParseService;

    private EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager;

    private LiveRoomData liveRoomData;
    /**
     * 启用定时器的客户端
     * @param serverUri 需要链接的URI
     * @param httpHeaders 请求头
     * @param connectTimeout 链接超时时间
     * @param intervalSecond 定时执行时间(秒)
     * @param intervalSendStringMessage 定时发送字符串信息
     * @param danMuParseService
     * @param liveRoomData
     */
    public BaseWebSocketClientByJdk(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, Integer intervalSecond, String intervalSendStringMessage, DanMuParseService danMuParseService, byte[] handshakeDataByteArray, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, LiveRoomData liveRoomData) throws DeploymentException, IOException, ExecutionException, InterruptedException, TimeoutException {
        this.intervalSendStringMessage = intervalSendStringMessage;
        this.handshakeDataByteArray = handshakeDataByteArray;
        this.liveRoomData = liveRoomData;
        //考虑定时器创建是否对外开放
        webSocketInterval = new WebSocketInterval(this,intervalSecond);
        this.intervalSecond = intervalSecond;
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
        this.eventManager = eventManager;
        this.connectTimeout = connectTimeout;
        this.httpHeaders = httpHeaders;
        createClient();
    }

    /**
     * 启用定时器的客户端
     * @param serverUri                 需要链接的URI
     * @param httpHeaders               请求头
     * @param connectTimeout            链接超时时间
     * @param intervalSecond            定时执行时间(秒)
     * @param intervalSendStringByteArray 定时发送byte[]信息
     * @param danMuParseService         弹幕解析实现类
     * @param handshakeDataByteArray    握手时发送数据
     * @param liveRoomData
     */
    public BaseWebSocketClientByJdk(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, Integer intervalSecond, byte[] intervalSendStringByteArray, DanMuParseService danMuParseService, byte[] handshakeDataByteArray, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, LiveRoomData liveRoomData) throws DeploymentException, IOException, ExecutionException, InterruptedException, TimeoutException {
        this.liveRoomData = liveRoomData;
        this.intervalSendStringByteArray = intervalSendStringByteArray;
        //考虑定时器创建是否对外开放
        webSocketInterval = new WebSocketInterval(this,intervalSecond);
        this.intervalSecond = intervalSecond;
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
        this.handshakeDataByteArray = handshakeDataByteArray;
        this.eventManager = eventManager;
        this.connectTimeout = connectTimeout;
        this.httpHeaders = httpHeaders;
        createClient();
    }
    /**
     * 启用定时器的客户端
     * @param serverUri 需要链接的URI
     * @param httpHeaders 请求头
     * @param connectTimeout 链接超时时间
     * @param intervalSecond 定时执行时间(秒)
     * @param intervalSendStringByteArray 定时发送byte[]信息
     * @param danMuParseService
     * @param liveRoomData
     */
    public BaseWebSocketClientByJdk(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, Integer intervalSecond, byte[] intervalSendStringByteArray, DanMuParseService danMuParseService, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, LiveRoomData liveRoomData) throws DeploymentException, IOException, ExecutionException, InterruptedException, TimeoutException {
        this.intervalSendStringByteArray = intervalSendStringByteArray;
        this.liveRoomData = liveRoomData;
        //考虑定时器创建是否对外开放
        webSocketInterval = new WebSocketInterval(this,intervalSecond);
        this.intervalSecond = intervalSecond;
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
        this.eventManager = eventManager;
        this.connectTimeout = connectTimeout;
        this.httpHeaders = httpHeaders;
        createClient();
    }

    /**
     * 构建并设置 WebSocketClient 实例，调用connect方法以链接指定URI
     * Constructs a WebSocketClient instance and sets it to the connect to the specified URI. The
     * channel does not attampt to connect automatically. The connection will be established once you
     * call <var>connect</var>.
     *  @param serverUri         需要链接的URI   the server URI to connect to
     * @param httpHeaders       请求头    Additional HTTP-Headers
     * @param connectTimeout    链接超时时间    The Timeout for the connection
     * @param danMuParseService
     * @param liveRoomData
     */
    public BaseWebSocketClientByJdk(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, DanMuParseService danMuParseService, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, LiveRoomData liveRoomData) throws DeploymentException, IOException, ExecutionException, InterruptedException, TimeoutException {
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
        this.eventManager = eventManager;
        this.liveRoomData = liveRoomData;
        this.connectTimeout = connectTimeout;
        this.httpHeaders = httpHeaders;
        createClient();
    }
    /**
     * 定时执行
     */
    @Override
    public void intervalRun() {
        if (intervalSendStringByteArray != null) {
            logger.debug("发送定时数据:{}",intervalSendStringByteArray);
            ByteBuffer data = ByteBuffer.wrap(intervalSendStringByteArray);
            webSocket.sendBinary(data, true);
        } else if (intervalSendStringMessage != null) {
            logger.debug("发送定时消息:{}",intervalSendStringMessage);
            webSocket.sendText(intervalSendStringMessage, true);
        }
    }

    private void createClient(){
        httpClientBuilder = HttpClient.newBuilder();
    }

    public void setProxy(InetSocketAddress proxy) {
        httpClientBuilder.proxy(ProxySelector.of(proxy));
    }

    public void connect() throws ExecutionException, InterruptedException {
        //重定向策略
        httpClientBuilder.followRedirects(HttpClient.Redirect.ALWAYS);
        //httpclient与ws客户端构建
        httpClient = httpClientBuilder.build();
        wsBuilder = httpClient.newWebSocketBuilder();
        //ws请求头
        for (Map.Entry<String, String> entry : httpHeaders.entrySet()
        ) {
            wsBuilder.header(entry.getKey(), entry.getValue());
        }
        //建立连接
        webSocketCompletableFuture = wsBuilder.buildAsync(serverUri, new BaseWebSocketListener(liveRoomData, eventManager, webSocketInterval, scheduledExecutorService, handshakeDataByteArray, danMuParseService));
        webSocket = webSocketCompletableFuture.get();
    }
}
