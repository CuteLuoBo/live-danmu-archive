//package com.github.cuteluobo.livedanmuarchive.websocketclient;
//
//import com.github.cuteluobo.livedanmuarchive.enums.DanMuMessageType;
//import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
//import com.github.cuteluobo.livedanmuarchive.pojo.HuyaDanMuData;
//import com.github.cuteluobo.livedanmuarchive.service.DanMuExportService;
//import com.github.cuteluobo.livedanmuarchive.utils.ByteUtil;
//import com.qq.tars.protocol.tars.TarsInputStream;
//import org.java_websocket.handshake.ServerHandshake;
//
//import java.net.URI;
//import java.nio.ByteBuffer;
//import java.util.Map;
//
///**
// * 废弃
// * 虎牙ws客户都
// * @author CuteLuoBo
// * @date 2021/12/17 16:39
// */
//public class HuyaWebSocketClient {
//
//    /**
//     * 心跳包
//     */
//    private static String heartbeat = "00031d0000690000006910032c3c4c56086f6e6c696e657569660f4f6e557365724865617274426561747d00003c0800010604745265711d00002f0a0a0c1600260036076164725f77617046000b1203aef00f2203aef00f3c426d5202605c60017c82000bb01f9cac0b8c980ca80c";
//    private static byte[] heartbeatByteArray = ByteUtil.parseHexString(heartbeat);
//    private DanMuExportService danMuExportService;
//
//    /**
//     * 构建并设置 WebSocketClient 实例，调用connect方法以链接指定URI
//     * Constructs a WebSocketClient instance and sets it to the connect to the specified URI. The
//     * channel does not attampt to connect automatically. The connection will be established once you
//     * call <var>connect</var>.
//     *  @param serverUri      需要链接的URI   the server URI to connect to
//     * @param httpHeaders    请求头    Additional HTTP-Headers
//     * @param connectTimeout 链接超时时间    The Timeout for the connection
//     * @param danMuExportService
//     */
//    public HuyaWebSocketClient(URI serverUri, Map<String, String> httpHeaders, int connectTimeout, DanMuExportService danMuExportService) {
////        super(serverUri, httpHeaders, connectTimeout,60,heartbeatByteArray, danMuParseService);
//        this.danMuExportService = danMuExportService;
//    }
//
//    /**
//     * WebSocket 握手并链接 之后执行的回调
//     * Called after an opening handshake has been performed and the given websocket is ready to be
//     * written on.
//     *
//     * @param handshakedata 返回的握手数据 The handshake of the websocket instance
//     */
//    public void onOpen(ServerHandshake handshakedata) {
//        //TODO 调用获取房间号的函数，并发送包装后的信息
//        super.onOpen(handshakedata);
//    }
//
//    /**
//     * 从远程主机接收到byteBuffer后执行的回调
//     *
//     * @param byteBuffer 接收到的UTF-8解码消息 The UTF-8 decoded message that was received.
//     * @see #onMessage(ByteBuffer)
//     **/
//    public void onMessage(ByteBuffer byteBuffer) {
//        TarsInputStream tarsInputStream = new TarsInputStream(byteBuffer);
//        DanMuData danMuData = new HuyaDanMuData();
//        if (tarsInputStream.read(0, 0, false) == 7) {
//            //此处传byte[0]，表示让返回值返回为byte[]类型，实际输出与传入数组无关
//            tarsInputStream = new TarsInputStream(tarsInputStream.read(new byte[0], 1, false));
//            //real-url中本身是传Int64类型，应该可用int类型代替
//            if (tarsInputStream.read(0, 1, false) == 1400) {
//                tarsInputStream = new TarsInputStream(tarsInputStream.read(new byte[0], 2, false));
//                //默认为GBK，需转为utf-8
//                tarsInputStream.setServerEncoding("utf-8");
//                //设置发送用户
//                danMuData.setUserName(tarsInputStream.read(readUserData(byteBuffer), 0, false));
//                //内容
//                danMuData.setContent(tarsInputStream.read("", 3, false));
//                //获取颜色
//                int colorData = (Integer) tarsInputStream.read(readColorData(byteBuffer), 0, false);
//                if (colorData == -1) {
//                    colorData = 16777215;
//                }
//                danMuData.setColor(String.valueOf(colorData));
//            }
//        }
//        //设置收到的消息类型
//        if (danMuData.getUserName() == null) {
//            danMuData.setMsgType(DanMuMessageType.DAN_MU.getText());
//        } else {
//            danMuData.setMsgType(DanMuMessageType.OTHER.getText());
//        }
//        danMuExportService.saveMessageToFile(danMuData);
//        logger.debug("解析的弹幕消息:{}",danMuData);
//    }
//
//
//
//}
