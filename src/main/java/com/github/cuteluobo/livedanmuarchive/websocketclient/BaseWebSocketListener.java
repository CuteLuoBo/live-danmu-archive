package com.github.cuteluobo.livedanmuarchive.websocketclient;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuClientEventType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.listener.result.DanMuClientEventResult;
import com.github.cuteluobo.livedanmuarchive.manager.EventManager;
import com.github.cuteluobo.livedanmuarchive.pojo.LiveRoomData;
import com.github.cuteluobo.livedanmuarchive.service.DanMuParseService;
import com.github.cuteluobo.livedanmuarchive.utils.WebSocketInterval;
import org.java_websocket.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 *
 * jdk11+原生实现的WS客户端监听器
 * 参考 https://www.jianshu.com/p/ecf6ac4aa848
 * @author CuteLuoBo
 * @date 2022/4/20 20:57
 */
public class BaseWebSocketListener implements WebSocket.Listener {
    Logger logger = LoggerFactory.getLogger(getClass());
    private LiveRoomData liveRoomData;
    private EventManager<DanMuClientEventType,DanMuClientEventResult> eventManager;
    private WebSocketInterval webSocketInterval;

    private ScheduledExecutorService scheduledExecutorService;
    private byte[] handshakeDataByteArray;

    private long debugInfoTotal = 1;
    private long debugPingPongTotal = 0;
    /**
     * 弹幕解析实现类
     */
    private DanMuParseService danMuParseService;

    public BaseWebSocketListener(LiveRoomData liveRoomData, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, WebSocketInterval webSocketInterval, ScheduledExecutorService scheduledExecutorService, byte[] handshakeDataByteArray, DanMuParseService danMuParseService) {
        this.liveRoomData = liveRoomData;
        this.eventManager = eventManager;
        this.webSocketInterval = webSocketInterval;
        this.scheduledExecutorService = scheduledExecutorService;
        this.handshakeDataByteArray = handshakeDataByteArray;
        this.danMuParseService = danMuParseService;
    }

    /**
     * A {@code WebSocket} has been connected.
     *
     * <p> This is the initial invocation and it is made once. It is
     * typically used to make a request for more invocations.
     *
     * @param webSocket
     * @implSpec The default implementation is equivalent to:
     * <pre>{@code     webSocket.request(1); }</pre>
     */
    @Override
    public void onOpen(WebSocket webSocket) {
        logger.debug("ws客户端开始执行握手");
        if (handshakeDataByteArray != null) {
            logger.debug("任务：{}，ws客户端发送握手数据:{}",liveRoomData.getSaveName(),handshakeDataByteArray);
            ByteBuffer byteBuffer = ByteBuffer.wrap(handshakeDataByteArray);
            webSocket.sendBinary(byteBuffer, true);
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
            scheduledExecutorService.scheduleAtFixedRate(webSocketInterval,webSocketInterval.getIntervalTime(),webSocketInterval.getIntervalTime(), TimeUnit.SECONDS);
        }
        WebSocket.Listener.super.onOpen(webSocket);
    }

    /**
     * A textual data has been received.
     *
     * <p> Return a {@code CompletionStage} which will be used by the
     * {@code WebSocket} as an indication it may reclaim the
     * {@code CharSequence}. Do not access the {@code CharSequence} after
     * this {@code CompletionStage} has completed.
     *
     * @param webSocket the WebSocket on which the data has been received
     * @param data      the data
     * @param last      whether this invocation completes the message
     * @return a {@code CompletionStage} which completes when the
     * {@code CharSequence} may be reclaimed; or {@code null} if it may be
     * reclaimed immediately
     * @implSpec The default implementation is equivalent to:
     * <pre>{@code     webSocket.request(1);
     *    return null; }</pre>
     * @implNote The {@code data} is always a legal UTF-16 sequence.
     */
    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String dataString = String.valueOf(data);
        logger.debug("接收文本消息:{}",dataString);
        danMuParseService.parseMessage(dataString);
        webSocket.request(1);
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    /**
     * A binary data has been received.
     *
     * <p> This data is located in bytes from the buffer's position to its
     * limit.
     *
     * <p> Return a {@code CompletionStage} which will be used by the
     * {@code WebSocket} as an indication it may reclaim the
     * {@code ByteBuffer}. Do not access the {@code ByteBuffer} after
     * this {@code CompletionStage} has completed.
     *
     * @param webSocket the WebSocket on which the data has been received
     * @param data      the data
     * @param last      whether this invocation completes the message
     * @return a {@code CompletionStage} which completes when the
     * {@code ByteBuffer} may be reclaimed; or {@code null} if it may be
     * reclaimed immediately
     * @implSpec The default implementation is equivalent to:
     * <pre>{@code     webSocket.request(1);
     *    return null; }</pre>
     */
    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        try {
            debugInfoTotal++;
            danMuParseService.parseMessage(data);
            logger.debug("debugInfoTotal：{}",debugInfoTotal);
            logger.debug("debugPingPongTotal：{}",debugPingPongTotal);
        } catch (ServiceException exception) {
            logger.error("弹幕消息转换失败",exception);
        }
        webSocket.request(1);
        return WebSocket.Listener.super.onBinary(webSocket, data,last);
    }

    /**
     * A Ping message has been received.
     *
     * <p> As guaranteed by the WebSocket Protocol, the message consists of
     * not more than {@code 125} bytes. These bytes are located from the
     * buffer's position to its limit.
     *
     * <p> Given that the WebSocket implementation will automatically send a
     * reciprocal pong when a ping is received, it is rarely required to
     * send a pong message explicitly when a ping is received.
     *
     * <p> Return a {@code CompletionStage} which will be used by the
     * {@code WebSocket} as a signal it may reclaim the
     * {@code ByteBuffer}. Do not access the {@code ByteBuffer} after
     * this {@code CompletionStage} has completed.
     *
     * @param webSocket the WebSocket on which the message has been received
     * @param message   the message
     * @return a {@code CompletionStage} which completes when the
     * {@code ByteBuffer} may be reclaimed; or {@code null} if it may be
     * reclaimed immediately
     * @implSpec The default implementation is equivalent to:
     * <pre>{@code     webSocket.request(1);
     *    return null; }</pre>
     */
    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
//        webSocket.sendPong(message);
        debugPingPongTotal += 2;
        debugInfoTotal++;
        return WebSocket.Listener.super.onPing(webSocket, message);
    }

    /**
     * A Pong message has been received.
     *
     * <p> As guaranteed by the WebSocket Protocol, the message consists of
     * not more than {@code 125} bytes. These bytes are located from the
     * buffer's position to its limit.
     *
     * <p> Return a {@code CompletionStage} which will be used by the
     * {@code WebSocket} as a signal it may reclaim the
     * {@code ByteBuffer}. Do not access the {@code ByteBuffer} after
     * this {@code CompletionStage} has completed.
     *
     * @param webSocket the WebSocket on which the message has been received
     * @param message   the message
     * @return a {@code CompletionStage} which completes when the
     * {@code ByteBuffer} may be reclaimed; or {@code null} if it may be
     * reclaimed immediately
     * @implSpec The default implementation is equivalent to:
     * <pre>{@code     webSocket.request(1);
     *    return null; }</pre>
     */
    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        return WebSocket.Listener.super.onPong(webSocket, message);
    }

    /**
     * Receives a Close message indicating the WebSocket's input has been
     * closed.
     *
     * <p> This is the last invocation from the specified {@code WebSocket}.
     * By the time this invocation begins the WebSocket's input will have
     * been closed.
     *
     * <p> A Close message consists of a status code and a reason for
     * closing. The status code is an integer from the range
     * {@code 1000 <= code <= 65535}. The {@code reason} is a string which
     * has a UTF-8 representation not longer than {@code 123} bytes.
     *
     * <p> If the WebSocket's output is not already closed, the
     * {@code CompletionStage} returned by this method will be used as an
     * indication that the WebSocket's output may be closed. The WebSocket
     * will close its output at the earliest of completion of the returned
     * {@code CompletionStage} or invoking either of the {@code sendClose}
     * or {@code abort} methods.
     *
     * @param webSocket  the WebSocket on which the message has been received
     * @param statusCode the status code
     * @param reason     the reason
     * @return a {@code CompletionStage} which completes when the
     * {@code WebSocket} may be closed; or {@code null} if it may be
     * closed immediately
     * @apiNote Returning a {@code CompletionStage} that never completes,
     * effectively disables the reciprocating closure of the output.
     *
     * <p> To specify a custom closure code or reason code the
     * {@code sendClose} method may be invoked from inside the
     * {@code onClose} invocation:
     * <pre>{@code     public CompletionStage<?> onClose(WebSocket webSocket,
     *                                      int statusCode,
     *                                      String reason) {
     *        webSocket.sendClose(CUSTOM_STATUS_CODE, CUSTOM_REASON);
     *        return new CompletableFuture<Void>();
     *    } } </pre>
     * @implSpec The default implementation of this method returns
     * {@code null}, indicating that the output should be closed
     * immediately.
     */
    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        //直播停止时停止日志:ws连接关闭,code:1006,reason:,remote:true
        logger.info("任务：{}，ws连接关闭,code:{},reason:{}",liveRoomData.getSaveName(),statusCode,reason);
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
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    /**
     * An error has occurred.
     *
     * <p> This is the last invocation from the specified WebSocket. By the
     * time this invocation begins both the WebSocket's input and output
     * will have been closed. A WebSocket may invoke this method on the
     * associated listener at any time after it has invoked {@code onOpen},
     * regardless of whether or not any invocations have been requested from
     * the WebSocket.
     *
     * <p> If an exception is thrown from this method, resulting behavior is
     * undefined.
     *
     * @param webSocket the WebSocket on which the error has occurred
     * @param error
     */
    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        logger.error("{} 直播间ws连接出现错误：{}",liveRoomData.getWebsiteType().getName()+"-"+liveRoomData.getLiveRoomCode()+"-"+liveRoomData.getLiveAnchorName(), error);
        if (eventManager != null) {
            DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
            danMuClientEventResult.setMessage("直播间ws连接出现错误："+liveRoomData.getWebsiteType().getName()+"-"+liveRoomData.getLiveRoomCode()+"-"+liveRoomData.getLiveAnchorName());
            danMuClientEventResult.setLiveRoomData(liveRoomData);
            eventManager.notify(DanMuClientEventType.ERROR,danMuClientEventResult);
        }
        WebSocket.Listener.super.onError(webSocket, error);
    }
}
