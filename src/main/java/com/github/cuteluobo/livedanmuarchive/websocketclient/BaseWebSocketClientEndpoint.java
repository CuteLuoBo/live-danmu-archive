package com.github.cuteluobo.livedanmuarchive.websocketclient;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuClientEventType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.listener.result.DanMuClientEventResult;
import com.github.cuteluobo.livedanmuarchive.manager.EventManager;
import com.github.cuteluobo.livedanmuarchive.pojo.LiveRoomData;
import com.github.cuteluobo.livedanmuarchive.service.DanMuParseService;
import com.github.cuteluobo.livedanmuarchive.utils.WebSocketInterval;
import jakarta.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Tyrus方式实现websocket客户端处理模式
 *
 * @author CuteLuoBo
 * @date 2022/4/20 16:02
 */
@ClientEndpoint
public class BaseWebSocketClientEndpoint extends Endpoint {
    Logger logger = LoggerFactory.getLogger(getClass());
    private LiveRoomData liveRoomData;
    private EventManager<DanMuClientEventType,DanMuClientEventResult> eventManager;
    private WebSocketInterval webSocketInterval;
    private byte[] handshakeDataByteArray;
    /**
     * 弹幕解析实现类
     */
    private DanMuParseService danMuParseService;

    public BaseWebSocketClientEndpoint(LiveRoomData liveRoomData, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager, WebSocketInterval webSocketInterval, byte[] handshakeDataByteArray, DanMuParseService danMuParseService) {
        this.liveRoomData = liveRoomData;
        this.eventManager = eventManager;
        this.webSocketInterval = webSocketInterval;
        this.handshakeDataByteArray = handshakeDataByteArray;
        this.danMuParseService = danMuParseService;
    }

    /**
     * Developers must implement this method to be notified when a new conversation has just begun.
     * <p>
     * Note:
     * <ul>
     * <li>It is permitted to send messages from this method.</li>
     * <li>It is permitted to add {@link MessageHandler}s from this method. No messages will be
     *     mapped to the appropriate {@link MessageHandler} until this method has completed.</li>
     * </ul>
     *  @param session the session that has just been activated.
     *
     * @param config the configuration used to configure this endpoint.
     */
    @Override
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        if (handshakeDataByteArray != null) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(handshakeDataByteArray);
            try {
                session.getBasicRemote().sendBinary(byteBuffer);
            } catch (IOException ioException) {
                logger.error("发送握手包数据失败",ioException);
            }

        }
        session.addMessageHandler(new jakarta.websocket.MessageHandler.Whole<ByteBuffer>() {
            /**
             * Called when the message has been fully received.
             *
             * @param message the message data.
             */
            @Override
            @OnMessage
            public void onMessage(ByteBuffer message) {
                try {
                    danMuParseService.parseMessage(message);
                } catch (ServiceException exception) {
                    logger.error("弹幕消息转换失败",exception);
                }
            }
        });
        session.addMessageHandler((jakarta.websocket.MessageHandler.Whole<String>) s -> danMuParseService.parseMessage(s));
        if (webSocketInterval != null) {

        }
    }

    @Override
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);
        logger.info("任务：{}，ws连接关闭,code:{},reason:{}",liveRoomData.getSaveName(),closeReason.getCloseCode(),closeReason.getReasonPhrase());
    }

    /**
     * Developers may implement this method when the web socket session creates some kind of error that is not modeled
     * in the web socket protocol. This may for example be a notification that an incoming message is too big to handle,
     * or that the incoming message could not be encoded.
     *
     * <p>
     * There are a number of categories of exception that this method is (currently) defined to handle:
     * <ul>
     * <li>connection problems, for example, a socket failure that occurs before the web socket connection can be
     * formally closed. These are modeled as {@link SessionException}s</li>
     * <li>runtime errors thrown by developer created message handlers calls.</li>
     * <li>conversion errors encoding incoming messages before any message handler has been called. These are modeled as
     * {@link DecodeException}s</li>
     * </ul>
     *  @param session the session in use when the error occurs.
     *
     * @param thr the throwable representing the problem.
     */
    @Override
    @OnError
    public void onError(Session session, Throwable thr) {
        super.onError(session, thr);
        logger.error("{} 直播间ws连接出现错误：{}",liveRoomData.getWebsiteType().getName()+"-"+liveRoomData.getLiveRoomCode()+"-"+liveRoomData.getLiveAnchorName(), thr);
        if (eventManager != null) {
            DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
            danMuClientEventResult.setMessage("直播间ws连接出现错误："+liveRoomData.getWebsiteType().getName()+"-"+liveRoomData.getLiveRoomCode()+"-"+liveRoomData.getLiveAnchorName());
            danMuClientEventResult.setLiveRoomData(liveRoomData);
            eventManager.notify(DanMuClientEventType.ERROR,danMuClientEventResult);
        }
    }
}
