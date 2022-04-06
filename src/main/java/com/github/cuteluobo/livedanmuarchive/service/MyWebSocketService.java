package com.github.cuteluobo.livedanmuarchive.service;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * @author CuteLuoBo
 * @date 2021/12/16 14:40
 */
public abstract class MyWebSocketService extends WebSocketClient {
    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the specified URI. The
     * channel does not attampt to connect automatically. The connection will be established once you
     * call <var>connect</var>.
     *
     * @param serverUri the server URI to connect to
     */
    public MyWebSocketService(URI serverUri) {
        super(serverUri);
    }

    /**
     * Called after an opening handshake has been performed and the given websocket is ready to be
     * written on.
     *
     * @param handshakedata The handshake of the websocket instance
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    /**
     * Callback for string messages received from the remote host
     *
     * @param message The UTF-8 decoded message that was received.
     * @see #onMessage(ByteBuffer)
     **/
    @Override
    public void onMessage(String message) {

    }

    /**
     * Called after the websocket connection has been closed.
     *
     * @param code   The codes can be looked up here: {@link CloseFrame}
     * @param reason Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote
     **/
    @Override
    public void onClose(int code, String reason, boolean remote) {

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

    }
}
