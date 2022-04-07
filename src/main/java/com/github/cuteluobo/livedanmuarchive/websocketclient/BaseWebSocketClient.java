package com.github.cuteluobo.livedanmuarchive.websocketclient;

import com.github.cuteluobo.livedanmuarchive.service.DanMuParseService;
import com.github.cuteluobo.livedanmuarchive.utils.WebSocketInterval;
import lombok.SneakyThrows;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.*;

/**
 * 基础ws客户端
 * @author CuteLuoBo
 * @date 2021/12/16 14:42
 */
public class BaseWebSocketClient extends WebSocketClient {
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

    /**
     * 启用定时器的客户端
     * @param serverUri 需要链接的URI
     * @param httpHeaders 请求头
     * @param connectTimeout 链接超时时间
     * @param intervalSecond 定时执行时间(秒)
     * @param intervalSendStringMessage 定时发送字符串信息
     * @param danMuParseService
     */
    public BaseWebSocketClient(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, Integer intervalSecond, String intervalSendStringMessage, DanMuParseService danMuParseService) {

        //使用默认推荐的Draft_6455
        super(serverUri, new Draft_6455(), httpHeaders, connectTimeout);
        this.intervalSendStringMessage = intervalSendStringMessage;
        //考虑定时器创建是否对外开放
        webSocketInterval = new WebSocketInterval(this);
        this.intervalSecond = intervalSecond;
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
    }

    /**
     * 启用定时器的客户端
     *
     * @param serverUri                 需要链接的URI
     * @param httpHeaders               请求头
     * @param connectTimeout            链接超时时间
     * @param intervalSecond            定时执行时间(秒)
     * @param intervalSendStringByteArray 定时发送byte[]信息
     * @param danMuParseService         弹幕解析实现类
     * @param handshakeDataByteArray    握手时发送数据
     */
    public BaseWebSocketClient(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, Integer intervalSecond, byte[] intervalSendStringByteArray, DanMuParseService danMuParseService, byte[] handshakeDataByteArray) {

        //使用默认推荐的Draft_6455
        super(serverUri, new Draft_6455(), httpHeaders, connectTimeout);
        this.intervalSendStringMessage = intervalSendStringMessage;
        //考虑定时器创建是否对外开放
        webSocketInterval = new WebSocketInterval(this);
        this.intervalSecond = intervalSecond;
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
        this.handshakeDataByteArray = handshakeDataByteArray;
    }
    /**
     * 启用定时器的客户端
     * @param serverUri 需要链接的URI
     * @param httpHeaders 请求头
     * @param connectTimeout 链接超时时间
     * @param intervalSecond 定时执行时间(秒)
     * @param intervalSendStringByteArray 定时发送byte[]信息
     * @param danMuParseService
     */
    public BaseWebSocketClient(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, Integer intervalSecond, byte[] intervalSendStringByteArray, DanMuParseService danMuParseService) {
        //使用默认推荐的Draft_6455
        super(serverUri, new Draft_6455(), httpHeaders, connectTimeout);
        this.intervalSendStringByteArray = intervalSendStringByteArray;
        //考虑定时器创建是否对外开放
        webSocketInterval = new WebSocketInterval(this);
        this.intervalSecond = intervalSecond;
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
    }
    /**
     * 构建并设置 WebSocketClient 实例，调用connect方法以链接指定URI
     * Constructs a WebSocketClient instance and sets it to the connect to the specified URI. The
     * channel does not attampt to connect automatically. The connection will be established once you
     * call <var>connect</var>.
     *  @param serverUri      需要链接的URI   the server URI to connect to
     * @param httpHeaders    请求头    Additional HTTP-Headers
     * @param connectTimeout 链接超时时间    The Timeout for the connection
     * @param danMuParseService
     */
    public BaseWebSocketClient(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, DanMuParseService danMuParseService) {
        //使用默认推荐的Draft_6455
        //protocolDraft  WebSocket 协议请求模式  参考https://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
        super(serverUri, new Draft_6455(), httpHeaders, connectTimeout);
        this.serverUri = serverUri;
        this.danMuParseService = danMuParseService;
    }

    /**
     * 对外暴露的定时调用方法(建议由WebSocketInterval线程定时执行)
     */
    public void intervalSendData() {
        if (intervalSendStringByteArray != null) {
            send(intervalSendStringByteArray);
        } else if (intervalSendStringMessage != null) {
            send(intervalSendStringMessage);
        }
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
        //连接启用后定时发送指定数据
        if (webSocketInterval != null) {
            if (scheduledExecutorService == null) {
                //线程新建参考https://blog.csdn.net/qq_45186545/article/details/105715421
                scheduledExecutorService = new ScheduledThreadPoolExecutor(1,new NamedThreadFactory("web-socket-interval-pool"));
            }
            //立即发送一次后按当前设定延迟发送心跳包
            scheduledExecutorService.scheduleAtFixedRate(webSocketInterval,0,intervalSecond, TimeUnit.SECONDS);
            webSocketInterval.run();
        }
        logger.info("ws开始执行握手");
        logger.debug("当前连接URI：{}，返回握手状态数据:{}-{}",serverUri.toString(), handshakedata.getHttpStatus(), handshakedata.getHttpStatusMessage());
        if (handshakeDataByteArray != null) {
            send(handshakeDataByteArray);
            logger.info("ws发送握手数据");
        }else{
            logger.debug("未定义握手发送数据，忽略");
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
        logger.info("接收消息:{}",message);
        danMuParseService.parseMessage(message);
    }

    /**
     * 从远程主机接收到byteBuffer后执行的回调
     *
     * @param byteBuffer 接收到的UTF-8解码消息 The UTF-8 decoded message that was received.
     * @see #onMessage(ByteBuffer)
     **/
    @SneakyThrows
    @Override
    public void onMessage(ByteBuffer byteBuffer) {
        danMuParseService.parseMessage(byteBuffer);
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
        logger.info("ws连接关闭,code:{},reason:{},remote:{}",code,reason,remote);
        //TODO 使用观察者模式，对外通知此弹幕监听线程停止
        //关闭定时线程
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
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
        logger.error("ws连接出现错误：{}", ex);
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
}
