package com.github.cuteluobo.livedanmuarchive.websocketclient;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuClientEventType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.listener.result.DanMuClientEventResult;
import com.github.cuteluobo.livedanmuarchive.manager.EventManager;
import com.github.cuteluobo.livedanmuarchive.pojo.LiveRoomData;
import com.github.cuteluobo.livedanmuarchive.service.DanMuParseService;
import com.github.cuteluobo.livedanmuarchive.utils.WebSocketInterval;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketListener;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PongFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 基础ws客户端
 * @author CuteLuoBo
 * @date 2021/12/16 14:42
 */
public class BaseWebSocketClient extends WebSocketClient implements IntervalRun{
    Logger logger = LoggerFactory.getLogger(getClass());
    private WebSocketInterval webSocketInterval;
    private String intervalSendStringMessage = null;
    private byte[] intervalSendStringByteArray = null;
    private URI serverUri;
    private int intervalSecond = 60;
    private ScheduledExecutorService scheduledExecutorService;

    private byte[] handshakeDataByteArray;

    /**
     * 弹幕解析实现类
     */
    private DanMuParseService danMuParseService;

    private EventManager<DanMuClientEventType,DanMuClientEventResult> eventManager;

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
    public BaseWebSocketClient(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, Integer intervalSecond, String intervalSendStringMessage, DanMuParseService danMuParseService, byte[] handshakeDataByteArray, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, LiveRoomData liveRoomData) {

        //使用默认推荐的Draft_6455
        super(serverUri, new Draft_6455(), httpHeaders, connectTimeout);
        this.intervalSendStringMessage = intervalSendStringMessage;
        this.handshakeDataByteArray = handshakeDataByteArray;
        this.liveRoomData = liveRoomData;
        //考虑定时器创建是否对外开放
        webSocketInterval = new WebSocketInterval(this);
        this.intervalSecond = intervalSecond;
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
        this.eventManager = eventManager;
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
    public BaseWebSocketClient(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, Integer intervalSecond, byte[] intervalSendStringByteArray, DanMuParseService danMuParseService, byte[] handshakeDataByteArray, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, LiveRoomData liveRoomData) {

        //使用默认推荐的Draft_6455
        super(serverUri, new Draft_6455(), httpHeaders, connectTimeout);
        this.liveRoomData = liveRoomData;
        this.intervalSendStringByteArray = intervalSendStringByteArray;
        //考虑定时器创建是否对外开放
        webSocketInterval = new WebSocketInterval(this);
        this.intervalSecond = intervalSecond;
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
        this.handshakeDataByteArray = handshakeDataByteArray;
        this.eventManager = eventManager;
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
    public BaseWebSocketClient(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, Integer intervalSecond, byte[] intervalSendStringByteArray, DanMuParseService danMuParseService, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, LiveRoomData liveRoomData) {
        //使用默认推荐的Draft_6455
        super(serverUri, new Draft_6455(), httpHeaders, connectTimeout);
        this.intervalSendStringByteArray = intervalSendStringByteArray;
        this.liveRoomData = liveRoomData;
        //考虑定时器创建是否对外开放
        webSocketInterval = new WebSocketInterval(this);
        this.intervalSecond = intervalSecond;
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
        this.eventManager = eventManager;
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
    public BaseWebSocketClient(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, DanMuParseService danMuParseService, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, LiveRoomData liveRoomData) {
        //使用默认推荐的Draft_6455
        //protocolDraft  WebSocket 协议请求模式  参考https://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
        super(serverUri, new Draft_6455(), httpHeaders, connectTimeout);
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
        this.eventManager = eventManager;
        this.liveRoomData = liveRoomData;
    }



    /**
     * WebSocket 握手并链接 之后执行的回调
     * Called after an opening handshake has been performed and the given websocket is ready to be
     * written on.
     *
     * @param handshakedata 握手数据 The handshake of the websocket instance
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.debug("ws客户端开始执行握手");
        logger.debug("当前连接URI：{}，返回握手状态数据:{}-{}",serverUri.toString(), handshakedata.getHttpStatus(), handshakedata.getHttpStatusMessage());
        if (handshakeDataByteArray != null) {
            logger.debug("任务：{}，ws客户端发送握手数据:{}",liveRoomData.getSaveName(),handshakeDataByteArray);
            send(handshakeDataByteArray);
        }else{
            logger.debug("未定义握手发送数据，忽略");
        }
        if (eventManager != null) {
            DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
            danMuClientEventResult.setMessage("开始连接直播间ws"+liveRoomData.getWebsiteType().getName()+"-"+liveRoomData.getLiveRoomCode()+"-"+liveRoomData.getLiveAnchorName());
            danMuClientEventResult.setLiveRoomData(liveRoomData);
            eventManager.notify(DanMuClientEventType.START,danMuClientEventResult);
        }
        //连接启用后定时发送指定数据
        if (webSocketInterval != null) {
            if (scheduledExecutorService == null) {
                //线程新建参考https://blog.csdn.net/qq_45186545/article/details/105715421
                scheduledExecutorService = new ScheduledThreadPoolExecutor(1,new NamedThreadFactory("web-socket-interval-pool"));
            }
            //按当前设定延迟发送心跳包
            //04-20 BUG:解决B站直播弹幕因心跳包(?)导致的断线
            //描述：使用普通ws连接+代理时，发送心跳包后即刻断线，抓包中没有收到心跳返回包
            //04-20晚排查：，因无法回应ping请求被断线，正常程序会有返回pong
            //04-22 实现数据解包功能后正常(wss连接)，可能是其他操作被断线
            scheduledExecutorService.scheduleAtFixedRate(webSocketInterval,0,intervalSecond, TimeUnit.SECONDS);
//            webSocketInterval.run();
        }
    }

    /**
     * 从远程主机接收到字符串信息后执行的回调
     * Callback for string messages received from the remote host
     *
     * @param message 接收到的UTF-8解码消息 The UTF-8 decoded message that was received.
     * @see #onMessage(ByteBuffer)
     **/
    @Override
    public void onMessage(String message) {
        logger.debug("接收文本消息:{}",message);
        danMuParseService.parseMessage(message);
    }

    /**
     * This default implementation will send a pong in response to the received ping. The pong frame
     * will have the same payload as the ping frame.
     *
     * @param conn
     * @param f
     * @see WebSocketListener#onWebsocketPing(WebSocket, Framedata)
     */
    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        conn.sendFrame(new PongFrame());
    }

    /**
     * 从远程主机接收到byteBuffer后执行的回调
     *
     * @param byteBuffer 接收到的UTF-8解码消息 The UTF-8 decoded message that was received.
     * @see #onMessage(ByteBuffer)
     **/
    @Override
    public void onMessage(ByteBuffer byteBuffer) {
        try {
            danMuParseService.parseMessage(byteBuffer);
        } catch (ServiceException e) {
            logger.error("出现服务错误",e);
        }
    }

    /**
     * ws连接关闭后调用
     * Called after the websocket connection has been closed.
     *
     * @param code   The codes can be looked up here: {@link CloseFrame}
     * @param reason Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote
     **/
    @Override
    public void onClose(int code, String reason, boolean remote) {
        //直播停止时停止日志:ws连接关闭,code:1006,reason:,remote:true
        logger.info("任务：{}，ws连接关闭,code:{},reason:{},remote:{}",liveRoomData.getSaveName(),code,reason,remote);
        //关闭定时线程
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
        if (eventManager != null) {
            DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
            danMuClientEventResult.setMessage("直播间ws链接断开："+liveRoomData.getWebsiteType().getName()+"-"+liveRoomData.getLiveRoomCode()+"-"+liveRoomData.getLiveAnchorName());
            danMuClientEventResult.setLiveRoomData(liveRoomData);
            eventManager.notify(DanMuClientEventType.CLOSE,danMuClientEventResult);
        }
    }

    public EventManager<DanMuClientEventType, DanMuClientEventResult> getEventManager() {
        return eventManager;
    }

    public void setEventManager(EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * Called when errors occurs. If an error causes the websocket connection to fail {@link
     * #onClose(int, String, boolean)} will be called additionally.<br> This method will be called
     * primarily because of IO or protocol errors.<br> If the given exception is an RuntimeException
     * that probably means that you encountered a bug.<br>
     *
     * @param ex The exception causing this error
     **/
    @Override
    public void onError(Exception ex) {
        logger.error("{} 直播间ws连接出现错误：{}",liveRoomData.getWebsiteType().getName()+"-"+liveRoomData.getLiveRoomCode()+"-"+liveRoomData.getLiveAnchorName(), ex);
        if (eventManager != null) {
            DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
            danMuClientEventResult.setMessage("直播间ws连接出现错误："+liveRoomData.getWebsiteType().getName()+"-"+liveRoomData.getLiveRoomCode()+"-"+liveRoomData.getLiveAnchorName());
            danMuClientEventResult.setLiveRoomData(liveRoomData);
            eventManager.notify(DanMuClientEventType.ERROR,danMuClientEventResult);
        }
    }

    public URI getServerUri() {
        return serverUri;
    }

    public void setServerUri(URI serverUri) {
        this.serverUri = serverUri;
    }

    public byte[] getHandshakeDataByteArray() {
        return handshakeDataByteArray;
    }

    public void setHandshakeDataByteArray(byte[] handshakeDataByteArray) {
        this.handshakeDataByteArray = handshakeDataByteArray;
    }

    /**
     * 定时执行，对外暴露的定时调用方法(建议由WebSocketInterval线程定时执行)
     */
    @Override
    public void intervalRun() {
        if (intervalSendStringByteArray != null) {
            logger.debug("发送定时数据:{}",intervalSendStringByteArray);
            send(intervalSendStringByteArray);
//            sendPing();
        } else if (intervalSendStringMessage != null) {
            logger.debug("发送定时消息:{}",intervalSendStringMessage);
            send(intervalSendStringMessage);
//            sendPing();
        }
    }
}
