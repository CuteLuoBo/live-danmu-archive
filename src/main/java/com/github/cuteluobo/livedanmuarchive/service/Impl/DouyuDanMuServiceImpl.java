package com.github.cuteluobo.livedanmuarchive.service.Impl;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuClientEventType;
import com.github.cuteluobo.livedanmuarchive.enums.WebsiteType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.listener.result.DanMuClientEventResult;
import com.github.cuteluobo.livedanmuarchive.manager.EventManager;
import com.github.cuteluobo.livedanmuarchive.service.AbstractBaseDanMuService;
import com.github.cuteluobo.livedanmuarchive.service.DanMuExportService;
import com.github.cuteluobo.livedanmuarchive.service.Impl.parse.DouyuDanMuParseServiceImpl;
import com.github.cuteluobo.livedanmuarchive.utils.FormatUtil;
import com.github.cuteluobo.livedanmuarchive.utils.LinkUtil;
import com.github.cuteluobo.livedanmuarchive.websocketclient.BaseWebSocketClient;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 斗鱼弹幕服务类
 * @author CuteLuoBo
 * @date 2022/11/16 17:12
 */
public class DouyuDanMuServiceImpl extends AbstractBaseDanMuService {
    Logger logger = LoggerFactory.getLogger(DouyuDanMuServiceImpl.class);

    private static final String HEARTBEAT = "1400000014000000b102000074797065403d6d726b6c2f00";
    private static final String WS_URL = "wss://danmuproxy.douyu.com:8503/";

    /** 解析信息的匹配规则 **/
    private static final Pattern RID_PATTERN =Pattern.compile("\"rid\":(\\d{1,8})", Pattern.MULTILINE);
    private static final Pattern NIKE_PATTERN =Pattern.compile("\"nickname\":\"([^\"]*)\"", Pattern.MULTILINE);

    public DouyuDanMuServiceImpl(String liveRoomUrl, String saveName, DanMuExportService danMuExportService, EventManager<DanMuClientEventType, DanMuClientEventResult> eventManager) throws ServiceException, URISyntaxException {
        super(WebsiteType.Douyu, HEARTBEAT, liveRoomUrl, saveName, danMuExportService, eventManager);
    }


    /**
     * 交由子类实现的创建接口体方法
     *
     * @return 连接与解析指令
     * @throws Exception
     */
    @Override
    protected List<byte[]> createWebsocketCmdByteList() throws Exception {
        String interiorLiveRoomUrl = "https://m.douyu.com/" + getLiveRoomCode();
        String body = LinkUtil.getHtmlBody(interiorLiveRoomUrl);
        //主播名称
        Matcher liveAnchorMatcher = NIKE_PATTERN.matcher(body);
        if (liveAnchorMatcher.find()) {
            String nickName = liveAnchorMatcher.group(1);
            setLiveAnchorName(nickName);
            getLiveRoomData().setLiveAnchorName(nickName);
        }
        Matcher ridMatcher = RID_PATTERN.matcher(body);
        String rid;
        if (ridMatcher.find()) {
            rid = ridMatcher.group(1);
            logger.debug("rid:{}",rid);
        } else {
            //由监听器进行定时重试
            throw new ServiceException("直播间RID解析失败，无法继续");
        }
        //构建结构体
        //握手包参考：https://github.com/wbt5/real-url/blob/b6daf0abcd445b6ede76221a7d2aac383bc87a2e/danmu/danmaku/douyu.py
        //https://www.cnblogs.com/aadd123/p/14009467.html
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        List<byte[]> bytesList = new ArrayList<>(2);
        String requestData = "type@=loginreq/roomid@=" + rid + "/";
//        byte[] bytes1 = encodeMsg(requestData);
//        logger.debug("requestDataHex:{}",Hex.encodeHexString(bytes1));
//        byteArrayOutputStream.writeBytes(bytes1);

        byteArrayOutputStream.writeBytes(encodeMsg(requestData));
        bytesList.add(byteArrayOutputStream.toByteArray());
        byteArrayOutputStream.reset();

        String joinData = "type@=joingroup/rid@=" + rid + "/gid@=-9999/";
//        byte[] bytes2 = encodeMsg(joinData);
//        logger.debug("joinDataHex:{}",Hex.encodeHexString(bytes2));
//        byteArrayOutputStream.writeBytes(bytes2);
        byteArrayOutputStream.writeBytes(encodeMsg(joinData));
        bytesList.add(byteArrayOutputStream.toByteArray());
        byteArrayOutputStream.reset();

        byteArrayOutputStream.close();
        return bytesList;
    }

    /**
     * 开始录制--自定义接口
     *
     * @param danMuExportService 使用的导出接口
     * @throws InterruptedException 线程错误
     * @throws ServiceException     服务错误
     * @throws IOException          URL/IO错误
     * @throws URISyntaxException   URI解析错误
     */
    @Override
    public void startRecord(DanMuExportService danMuExportService) throws URISyntaxException, InterruptedException, ServiceException, IOException {
        WebSocketClient webSocketClient = null;
        try{
            if (initMessageParseRule()) {
                webSocketClient = new BaseWebSocketClient(new URI(WS_URL), LinkUtil.NORMAL_HEADER, 3600, 40, getHeartbeatByteArray()
                        , new DouyuDanMuParseServiceImpl(danMuExportService), getWebsocketCmdByteList(), getEventManager(), getLiveRoomData());
                //调试用
//                List<byte[]> debugHandBytesList = new ArrayList<>();
//                webSocketClient = new BaseWebSocketClient(new URI(WS_URL), LinkUtil.NORMAL_HEADER, 3600, 40, getHeartbeatByteArray()
//                        , new DouyuDanMuParseServiceImpl(danMuExportService), debugHandBytesList, getEventManager(), getLiveRoomData());
                //调试用proxy
//                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1",8888));
//                webSocketClient.setProxy(proxy);
                //https://github.com/TooTallNate/Java-WebSocket/issues/1059#issuecomment-669348480
                //禁用pong检查避免出现错误导致连接关闭 InvalidFrameException: Control frame can't have fin==false set
                webSocketClient.setConnectionLostTimeout( 0 );
                webSocketClient.connectBlocking();
            }
        } catch (Exception e) {
            DanMuClientEventResult danMuClientEventResult = new DanMuClientEventResult();
            danMuClientEventResult.setLiveRoomData(getLiveRoomData());
            if (webSocketClient != null) {
                //关闭并废弃ws连接
                webSocketClient.close();
                danMuClientEventResult.setWebsocketConnectClose(true);
            }
            danMuClientEventResult.setMessage("录制启动时出现错误");
            logger.error("任务: {},启动录制时出现错误：", getSaveName(), e);
            if (getEventManager() != null) {
                getEventManager().notify(DanMuClientEventType.CLOSE,danMuClientEventResult);
            }
        }
    }

    /**
     * 信息编码
     * @param msg 编码的数据
     * @return 编码完成后的字节数组
     * @throws IOException 字节流写入错误
     * @throws DecoderException Hex编码错误
     */
    private byte[] encodeMsg(String msg) throws IOException, DecoderException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        byte[] msgBytes = msg.getBytes(StandardCharsets.US_ASCII);
        int packLength = msgBytes.length + 9;
        //默认writeInt无效，需要转换
        byte[] packLengthBytes = FormatUtil.int2HighBytes(packLength);
        dataOutputStream.write(packLengthBytes);
        dataOutputStream.write(packLengthBytes);
        //689
        dataOutputStream.write(Hex.decodeHex("b1020000"));
        dataOutputStream.write(msgBytes);
        dataOutputStream.write(Hex.decodeHex("00"));
        return byteArrayOutputStream.toByteArray();
    }
}
