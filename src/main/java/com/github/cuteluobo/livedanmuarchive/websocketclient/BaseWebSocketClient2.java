package com.github.cuteluobo.livedanmuarchive.websocketclient;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuClientEventType;
import com.github.cuteluobo.livedanmuarchive.listener.result.DanMuClientEventResult;
import com.github.cuteluobo.livedanmuarchive.manager.EventManager;
import com.github.cuteluobo.livedanmuarchive.pojo.LiveRoomData;
import com.github.cuteluobo.livedanmuarchive.service.DanMuParseService;
import com.github.cuteluobo.livedanmuarchive.utils.WebSocketInterval;
import jakarta.websocket.*;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Tyrus方式实现websocket客户端
 * @author CuteLuoBo
 * @date 2022/4/20 16:25
 */
public class BaseWebSocketClient2 implements IntervalRun {
    Logger logger = LoggerFactory.getLogger(getClass());
    private WebSocketInterval webSocketInterval;
    private String intervalSendStringMessage = null;
    private byte[] intervalSendStringByteArray = null;
    private URI serverUri;
    private int intervalSecond = 60;
    private ScheduledExecutorService scheduledExecutorService;
    private int connectTimeout = 3600;

    private ClientEndpointConfig clientEndpointConfig;
    private ClientManager clientManager;

    private byte[] handshakeDataByteArray;

    private Session session;

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
    public BaseWebSocketClient2(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, Integer intervalSecond, String intervalSendStringMessage, DanMuParseService danMuParseService, byte[] handshakeDataByteArray, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, LiveRoomData liveRoomData) throws DeploymentException, IOException, ExecutionException, InterruptedException, TimeoutException {
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
    public BaseWebSocketClient2(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, Integer intervalSecond, byte[] intervalSendStringByteArray, DanMuParseService danMuParseService, byte[] handshakeDataByteArray, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, LiveRoomData liveRoomData) throws DeploymentException, IOException, ExecutionException, InterruptedException, TimeoutException {
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
    public BaseWebSocketClient2(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, Integer intervalSecond, byte[] intervalSendStringByteArray, DanMuParseService danMuParseService, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, LiveRoomData liveRoomData) throws DeploymentException, IOException, ExecutionException, InterruptedException, TimeoutException {
        this.intervalSendStringByteArray = intervalSendStringByteArray;
        this.liveRoomData = liveRoomData;
        //考虑定时器创建是否对外开放
        webSocketInterval = new WebSocketInterval(this,intervalSecond);
        this.intervalSecond = intervalSecond;
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
        this.eventManager = eventManager;
        this.connectTimeout = connectTimeout;
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
    public BaseWebSocketClient2(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, DanMuParseService danMuParseService, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, LiveRoomData liveRoomData) throws DeploymentException, IOException, ExecutionException, InterruptedException, TimeoutException {
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
        this.eventManager = eventManager;
        this.liveRoomData = liveRoomData;
        this.connectTimeout = connectTimeout;
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
            session.getAsyncRemote().sendBinary(data);
        } else if (intervalSendStringMessage != null) {
            logger.debug("发送定时消息:{}",intervalSendStringMessage);
            session.getAsyncRemote().sendText(intervalSendStringMessage);
        }
    }

    public void createClient() throws DeploymentException, ExecutionException, InterruptedException, TimeoutException, IOException {
        this.clientEndpointConfig = ClientEndpointConfig.Builder.create()
                .configurator(new ClientEndpointConfig.Configurator(){
                    @Override
                    public void beforeRequest(Map<String, List<String>> headers) {
//                        super.beforeRequest(headers);
                        headers = headers;
                    }
                })
                .build();
        this.clientManager = ClientManager.createClient();
        //启用握手记录日志
        clientManager.getProperties().put(ClientProperties.LOG_HTTP_UPGRADE, true);

    }

    public void setProxy(String proxyUri) {
        clientManager.getProperties().put(ClientProperties.PROXY_URI, proxyUri);
    }

    public void connect() throws DeploymentException, IOException, ExecutionException, InterruptedException, TimeoutException {
        CountDownLatch messageLatch = new CountDownLatch(1);
        WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        session = webSocketContainer.connectToServer(new BaseWebSocketClientEndpoint(liveRoomData, eventManager, webSocketInterval, handshakeDataByteArray, danMuParseService), serverUri);
        messageLatch.await(connectTimeout, TimeUnit.MILLISECONDS);
        //初始化连接
//        session = clientManager.connectToServer(new BaseWebSocketClientEndpoint(liveRoomData, eventManager, webSocketInterval, danMuParseService), serverUri);

//        Future<Session> sessionFuture = clientManager.asyncConnectToServer(new BaseWebSocketClientEndpoint(liveRoomData, eventManager, webSocketInterval, danMuParseService), serverUri);
//        this.session = sessionFuture.get(connectTimeout,TimeUnit.MILLISECONDS);
        //发送握手信息
        if (handshakeDataByteArray != null) {
            ByteBuffer handshakeData = ByteBuffer.wrap(handshakeDataByteArray);
            session.getAsyncRemote().sendBinary(handshakeData);
        }
    }
}
