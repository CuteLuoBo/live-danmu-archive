package com.github.cuteluobo.livedanmuarchive.service.Impl.parse;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuMessageType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuFormat;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuUserInfo;
import com.github.cuteluobo.livedanmuarchive.service.DanMuExportService;
import com.github.cuteluobo.livedanmuarchive.service.DanMuParseService;
import com.github.cuteluobo.livedanmuarchive.utils.FormatUtil;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 斗鱼弹幕解析实现类
 * @author CuteLuoBo
 * @date 2022/11/16 22:16
 */
public class DouyuDanMuParseServiceImpl implements DanMuParseService {
    private static final Pattern MSG_PATTERN =Pattern.compile("\"rid\":(\\d{1,8})", Pattern.MULTILINE);
    Logger logger = LoggerFactory.getLogger(DouyuDanMuParseServiceImpl.class);
    private DanMuExportService danMuExportService;

    public DouyuDanMuParseServiceImpl(DanMuExportService danMuExportService) {
        this.danMuExportService = danMuExportService;
    }

    /**
     * 解析返回的字符串类型信息
     *
     * @param message 字符串信息
     * @return 处理完成的弹幕信息
     */
    @Override
    public List<DanMuData> parseMessage(String message) {
        String testMessage = message;
        logger.debug("收到文字消息:{}",testMessage);
        return null;
    }

    /**
     * 解析返回的字节流类型信息
     *
     * @param byteBufferMessage
     * @return 处理完成的弹幕信息
     */
    @Override
    public List<DanMuData> parseMessage(ByteBuffer byteBufferMessage) throws ServiceException {
        logger.trace("收到字节消息,转Hex:{}",Hex.encodeHexString(byteBufferMessage));
        //转hex后重置索引
        byteBufferMessage.position(0);
        List<DanMuData> danMuDataList = new ArrayList<>(10);
        try (DataInputStream dataInputStream = new DataInputStream(new ByteBufferBackedInputStream(byteBufferMessage))) {
            //报文长度
            byte[] packetLengthByte = new byte[4];
            dataInputStream.read(packetLengthByte,0, 4);
            //跳过12字节
            dataInputStream.readNBytes(12);
            int packetLength = FormatUtil.highBytes2Int(packetLengthByte);
            byte[] bodyBytes = new byte[packetLength];
            dataInputStream.read(bodyBytes, 0, packetLength);
            String content = new String(bodyBytes, StandardCharsets.UTF_8);
            String[] msgArray = content.split("/");
            String chatType = "@=chatmsg";
            if (msgArray.length > 0) {
                //非弹幕消息不解析
                if (!msgArray[0].endsWith(chatType)) {
                    return null;
                }
                DanMuData danMuData = new DanMuData();
                danMuData.setMsgType(DanMuMessageType.DAN_MU.getText());
                DanMuUserInfo danMuUserInfo = new DanMuUserInfo();
                danMuData.setUserIfo(danMuUserInfo);
                //斗鱼弹幕样式需要贵族以上才能自定义，略过记录样式
                danMuData.setDanMuFormatData(DanMuFormat.NORMAL);
                for (String msg :
                        msgArray) {
                    //字符转义
                    msg = msg.replaceAll("@s", "/").replaceAll("@A", "@");
                    String[] temp = msg.split("@=");
                    logger.trace("msg:{}", msg);
                    if (temp.length > 1) {
                        String msgType = temp[0];
                        switch (msgType) {
                            case "uid":
                                danMuUserInfo.setUid(temp[1]);
                                break;
                            case "nn":
                                danMuUserInfo.setNickName(temp[1]);
                                break;
                            case "txt":
                                danMuData.setContent(temp[1]);
                                break;
                            case "cst":
                                danMuData.setTimestamp(Long.parseLong(temp[1]));
                                break;
                            default:
                        }
                    }
                }
                //使用本地系统时间进行记录
                danMuData.setTimestamp(System.currentTimeMillis());
                danMuDataList.add(danMuData);
                logger.debug("弹幕消息：{}:{},timestemp:{}", danMuData.getUserIfo().getNickName(), danMuData.getContent(), danMuData.getTimestamp());
                if (!danMuDataList.isEmpty()) {
                    try {
                        if (danMuDataList.size() == 1) {
                            danMuExportService.export(danMuDataList.get(0));
                        } else {
                            danMuExportService.batchExport(danMuDataList);
                        }
                    } catch (IOException ioException) {
                        logger.error("弹幕数据导出出现IO错误：", ioException);
                    }
                }
                return danMuDataList;
            }
            logger.trace("BodyBytesHex:{}", Hex.encodeHexString(bodyBytes));
        } catch (IOException ioException) {
            logger.error("数据包解包时出现IO错误：", ioException);
            return null;
        }
        return null;
    }
}
