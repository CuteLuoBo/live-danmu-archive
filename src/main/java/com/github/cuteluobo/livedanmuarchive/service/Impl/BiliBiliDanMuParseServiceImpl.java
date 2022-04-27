package com.github.cuteluobo.livedanmuarchive.service.Impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuMessageType;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuFormat;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuUserInfo;
import com.github.cuteluobo.livedanmuarchive.service.DanMuExportService;
import com.github.cuteluobo.livedanmuarchive.service.DanMuParseService;
import com.github.cuteluobo.livedanmuarchive.service.ExDanMuExportService;
import com.google.gson.JsonArray;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;
import org.apache.commons.compress.compressors.brotli.BrotliUtils;
import org.apache.commons.compress.utils.CountingInputStream;
import org.brotli.dec.BrotliInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * @author CuteLuoBo
 * @date 2022/4/17 16:46
 */
public class BiliBiliDanMuParseServiceImpl implements DanMuParseService {
    Logger logger = LoggerFactory.getLogger(BiliBiliDanMuParseServiceImpl.class);

    private DanMuExportService danMuExportService;

    public BiliBiliDanMuParseServiceImpl(DanMuExportService danMuExportService) {
        this.danMuExportService = danMuExportService;
    }

    /**
     * 解析返回的字符串类型信息
     *
     * @param Message 字符串信息
     * @return 处理完成的弹幕信息
     */
    @Override
    public List<DanMuData> parseMessage(String Message) {
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
        //初始化数据储存数组
        List<Integer> opsList = new ArrayList<>(10);
        List<String> danMuList = new ArrayList<>(10);
        List<ByteBuffer> danMuZlibCompressedList = new ArrayList<>(10);
        List<ByteBuffer> danMuBrotliCompressedList = new ArrayList<>();
        List<String> msgList = new ArrayList<>(10);

        List<DanMuData> danMuDataList = new ArrayList<>(10);

        //数据包解析部分
        //参考1:https://github.com/wbt5/real-url/blob/8b7635d2fcb0e104f97f64a4927ad537d6520ff0/danmu/danmaku/bilibili.py#L3
        //参考2:https://github.com/lovelyyoshino/Bilibili-Live-API/blob/master/API.WebSocket.md
        try (DataInputStream dataInputStream = new DataInputStream(new ByteBufferBackedInputStream(byteBufferMessage))) {
            //python unpack "!IHHII"
            //字符定义参考https://blog.csdn.net/weixin_44621343/article/details/112793520
            //java unpack解析方案参考：https://stackoverflow.com/a/12093013/18631563
            int packetLength = dataInputStream.readInt();
            int headerLength = dataInputStream.readUnsignedShort();
            //协议版本
            int ver = dataInputStream.readUnsignedShort();
            //操作类型
            int op = dataInputStream.readInt();
            int seq = dataInputStream.readInt();
            int bodyLength = packetLength - headerLength;
            byte[] bodyBytes = new byte[bodyLength];
//            byte[] packetByte = new byte[packetLength];
            dataInputStream.read(bodyBytes, 0, bodyLength);
            switch (ver) {
                //Brotli 压缩
                case 3:
                    danMuBrotliCompressedList.add(ByteBuffer.wrap(bodyBytes));
                    break;
                //zlib压缩
                case 2:
                    danMuZlibCompressedList.add(ByteBuffer.wrap(bodyBytes));
                    break;
                //心跳包数据
                case 1:
                    logger.debug("心跳包数据：{}", bodyBytes);
                    break;
                //未压缩数据
                default:
                case 0:
                    opsList.add(op);
                    danMuList.add(new String(bodyBytes, StandardCharsets.UTF_8));
                    break;
            }
            logger.debug("FirstBodyBytesHex:{}", Hex.encodeHexString(bodyBytes));
        } catch (IOException ioException) {
            logger.error("数据包解包时出现IO错误：", ioException);
            return null;
        }

        //zlib压缩数据处理
        for (ByteBuffer byteBuffer : danMuZlibCompressedList) {
//            try(DataInputStream decompressionDataStream =
//                        new DataInputStream(new InflaterInputStream(new BufferedInputStream(new ByteBufferBackedInputStream(byteBuffer) ))))
            int packetNum = 0;
            try(InflaterInputStream inflaterInputStream = new InflaterInputStream(new BufferedInputStream(new ByteBufferBackedInputStream(byteBuffer) )))
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
                int byteData = -1;
                while ((byteData = inflaterInputStream.read())!=-1){
                    bos.write(byteData);
                }
                inflaterInputStream.close();
                ByteBuffer tempByteBuffer = ByteBuffer.wrap(bos.toByteArray());
//                DataInputStream decompressionDataStream = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(bos.toByteArray())));
                bos.close();
                while (true){
                    int bufferStartPosition = tempByteBuffer.position();
                    int packetLength = tempByteBuffer.getInt();
                    int headerLength = tempByteBuffer.getShort();
                    int ver = tempByteBuffer.getShort();
                    int op = tempByteBuffer.getInt();
                    int seq = tempByteBuffer.getInt();
                    int bodyLength = packetLength - headerLength;

                    //debug
                    logger.debug("-------------------------------");
                    logger.debug("bufferStartPosition:{},bufferLimit:{},bufferCapacity:{},packetLength:{},headerLength:{},ver:{},op:{},seq:{}，bodyLength:{}"
                            ,bufferStartPosition,tempByteBuffer.limit(),tempByteBuffer.capacity(),packetLength,headerLength,ver,op,seq,bodyLength);
                    //可读取数据长度不符合包长度时
                    if (tempByteBuffer.limit()-tempByteBuffer.position() < bodyLength) {
                        logger.debug("标记跳出,limit:{},position:{},limit-position:{},packetLength:{}",
                                tempByteBuffer.limit(),tempByteBuffer.position(),tempByteBuffer.limit()-tempByteBuffer.position(),packetLength);
                        break;
                    }
                    //存储数据
                    opsList.add(op);
                    byte[] bytes = new byte[bodyLength];
                    tempByteBuffer.get(bytes, 0, bodyLength);
                    String bodyString = new String(bytes, StandardCharsets.UTF_8);
                    danMuList.add(bodyString);

                    logger.debug("zlibHex:{}",Hex.encodeHexString(bytes));
                    logger.debug("bodyString:{}",bodyString);
                    logger.debug("packetNum:{}",packetNum);
                    packetNum++;

                    //读取结束时
                    if (tempByteBuffer.position() == tempByteBuffer.limit()) {
                        break;
                    }
                }

            }catch (IOException ioException) {
                logger.warn("zlib解压数据错误",ioException);
                return null;
            }
        }
        //brotli，此方法弃用，报错BroTil stream decoding failed，在入房信息中将压缩模式调整为其他避过
        for (ByteBuffer brotliBytes :
                danMuBrotliCompressedList) {
            //读取全部后再读入
            ByteBuffer tempByteBuffer = ByteBuffer.allocate(0);
            try (BrotliInputStream brotliInputStream = new BrotliInputStream(new BufferedInputStream(new ByteBufferBackedInputStream(brotliBytes)))) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                int readByte = -1;
                while ((readByte = brotliInputStream.read()) != -1) {
                    byteArrayOutputStream.write(readByte);
                }
                brotliInputStream.close();
                byte[] bytes = byteArrayOutputStream.toByteArray();
                tempByteBuffer = ByteBuffer.wrap(bytes);
                byteArrayOutputStream.close();
            } catch (IOException ioException) {
                logger.error("brotli解压流出现IO错误：",ioException);
                return null;
            }
            try (DataInputStream decompressionDataStream = new DataInputStream(new BufferedInputStream(new ByteBufferBackedInputStream(tempByteBuffer)))){
                while (true){
                    int packetLength = decompressionDataStream.readInt();
                    int headerLength = decompressionDataStream.readUnsignedShort();
                    int ver = decompressionDataStream.readUnsignedShort();
                    int op = decompressionDataStream.readInt();
                    int seq = decompressionDataStream.readInt();
                    int bodyLength = packetLength - headerLength;
                    //可读取数据长度不符合包长度时
                    if (tempByteBuffer.limit()-tempByteBuffer.position() < packetLength) {
                        break;
                    }
                    opsList.add(op);
                    byte[] packetBytes = new byte[packetLength];
                    decompressionDataStream.read(packetBytes, 0, packetLength);
                    String bodyString = new String(packetBytes, StandardCharsets.UTF_8);
                    danMuList.add(bodyString);
                    logger.debug("bodyString:{}",bodyString);
                    //读取结束时
                    if (tempByteBuffer.position() == tempByteBuffer.limit()) {
                        break;
                    }
                }
            } catch (IOException ioException) {
                logger.warn("brotli解压数据错误",ioException);
                return null;
            }
        }
        //参考：https://www.bilibili.com/read/cv14101053
        //https://www.lyyyuna.com/2016/03/14/bilibili-danmu01/
        //https://github.com/lovelyyoshino/Bilibili-Live-API/blob/master/API.WebSocket.md
        //https://www.cxyzjd.com/article/johnchang96/53192352
        //https://daidr.me/archives/code-526.html

        //消息模式：https://github.com/BanqiJane/Bilibili_Danmuji/blob/7ca3d9f20f05c5f5ac2dbb46f422be4bfd6ca640/src/main/java/xyz/acproject/danmuji/thread/core/ParseMessageThread.java

        for (int i = 0; i < danMuList.size(); i++) {
            String body = danMuList.get(i);
            logger.debug("消息:{}",body);
            logger.debug("操作类型:{}",opsList.get(i));
            if (opsList.get(i) == 5) {
                //jackson解析
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode jsonNode = objectMapper.readTree(body);
                    JsonNode cmd = jsonNode.get("cmd");
                    //为Null是为数据包
                    if (cmd == null) {
                        logger.info("数据包信息：{}", body);
                    } else {
                        String messageType = cmd.asText();
                        DanMuData danMuData = new DanMuData();
                        JsonNode info = jsonNode.get("info");
                        if (messageType.startsWith("DANMU_MSG")){
                            messageType = "DANMU_MSG";
                        }
                        switch (messageType) {
                            case "DANMU_MSG":
                                //信息主体
                                danMuData.setMsgType(DanMuMessageType.DAN_MU.getText());
                                danMuData.setContent(info.get(1).asText());
                                //用户信息
                                DanMuUserInfo danMuUserInfo = new DanMuUserInfo();
                                danMuUserInfo.setNickName(info.get(2).get(1).asText());
                                danMuData.setUserIfo(danMuUserInfo);
                                danMuData.setTimestamp(info.get(9).get("ts").asLong());
                                //弹幕格式
                                DanMuFormat danMuFormat = new DanMuFormat();
                                String extra = info.get(0).get(15).get("extra").asText();
                                JsonNode extraJson = objectMapper.readTree(extra);
                                danMuFormat.setFontColor(extraJson.get("color").asInt());
                                danMuFormat.setFontSize(extraJson.get("font_size").asInt());
                                danMuData.setDanMuFormatData(danMuFormat);
                                logger.debug("danMuData:{}", danMuData);
                                danMuDataList.add(danMuData);
                                //后续增加其余识别功能时，恢复此break
                                //break;
                            default:
                                break;
                        }
                    }

                } catch (JsonProcessingException jsonParseException) {
                    logger.debug("error-index:{}",i);
                    logger.error("弹幕信息解析错误，跳过:{}", body);
                }

            }
        }
        if (!danMuDataList.isEmpty()) {
            if (danMuDataList.size() == 1) {
                try {
                    danMuExportService.export(danMuDataList.get(0));
                } catch (IOException ioException) {
                    logger.error("弹幕数据导出出现IO错误：", ioException);
                }
            } else {
                danMuExportService.batchExport(danMuDataList);
            }
        }
        return danMuDataList;
    }
}
