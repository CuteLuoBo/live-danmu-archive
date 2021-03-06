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
     * ????????????????????????????????????
     *
     * @param Message ???????????????
     * @return ???????????????????????????
     */
    @Override
    public List<DanMuData> parseMessage(String Message) {
        return null;
    }

    /**
     * ????????????????????????????????????
     *
     * @param byteBufferMessage
     * @return ???????????????????????????
     */
    @Override
    public List<DanMuData> parseMessage(ByteBuffer byteBufferMessage) throws ServiceException {
        //???????????????????????????
        List<Integer> opsList = new ArrayList<>(10);
        List<String> danMuList = new ArrayList<>(10);
        List<ByteBuffer> danMuZlibCompressedList = new ArrayList<>(10);
        List<ByteBuffer> danMuBrotliCompressedList = new ArrayList<>();
        List<String> msgList = new ArrayList<>(10);

        List<DanMuData> danMuDataList = new ArrayList<>(10);

        //?????????????????????
        //??????1:https://github.com/wbt5/real-url/blob/8b7635d2fcb0e104f97f64a4927ad537d6520ff0/danmu/danmaku/bilibili.py#L3
        //??????2:https://github.com/lovelyyoshino/Bilibili-Live-API/blob/master/API.WebSocket.md
        try (DataInputStream dataInputStream = new DataInputStream(new ByteBufferBackedInputStream(byteBufferMessage))) {
            //python unpack "!IHHII"
            //??????????????????https://blog.csdn.net/weixin_44621343/article/details/112793520
            //java unpack?????????????????????https://stackoverflow.com/a/12093013/18631563
            int packetLength = dataInputStream.readInt();
            int headerLength = dataInputStream.readUnsignedShort();
            //????????????
            int ver = dataInputStream.readUnsignedShort();
            //????????????
            int op = dataInputStream.readInt();
            int seq = dataInputStream.readInt();
            int bodyLength = packetLength - headerLength;
            byte[] bodyBytes = new byte[bodyLength];
//            byte[] packetByte = new byte[packetLength];
            dataInputStream.read(bodyBytes, 0, bodyLength);
            switch (ver) {
                //Brotli ??????
                case 3:
                    danMuBrotliCompressedList.add(ByteBuffer.wrap(bodyBytes));
                    break;
                //zlib??????
                case 2:
                    danMuZlibCompressedList.add(ByteBuffer.wrap(bodyBytes));
                    break;
                //???????????????
                case 1:
                    logger.debug("??????????????????{}", bodyBytes);
                    break;
                //???????????????
                default:
                case 0:
                    opsList.add(op);
                    danMuList.add(new String(bodyBytes, StandardCharsets.UTF_8));
                    break;
            }
            logger.debug("FirstBodyBytesHex:{}", Hex.encodeHexString(bodyBytes));
        } catch (IOException ioException) {
            logger.error("????????????????????????IO?????????", ioException);
            return null;
        }

        //zlib??????????????????
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
                    logger.debug("bufferStartPosition:{},bufferLimit:{},bufferCapacity:{},packetLength:{},headerLength:{},ver:{},op:{},seq:{}???bodyLength:{}"
                            ,bufferStartPosition,tempByteBuffer.limit(),tempByteBuffer.capacity(),packetLength,headerLength,ver,op,seq,bodyLength);
                    //??????????????????????????????????????????
                    if (tempByteBuffer.limit()-tempByteBuffer.position() < bodyLength) {
                        logger.debug("????????????,limit:{},position:{},limit-position:{},packetLength:{}",
                                tempByteBuffer.limit(),tempByteBuffer.position(),tempByteBuffer.limit()-tempByteBuffer.position(),packetLength);
                        break;
                    }
                    //????????????
                    opsList.add(op);
                    byte[] bytes = new byte[bodyLength];
                    tempByteBuffer.get(bytes, 0, bodyLength);
                    String bodyString = new String(bytes, StandardCharsets.UTF_8);
                    danMuList.add(bodyString);

                    logger.debug("zlibHex:{}",Hex.encodeHexString(bytes));
                    logger.debug("bodyString:{}",bodyString);
                    logger.debug("packetNum:{}",packetNum);
                    packetNum++;

                    //???????????????
                    if (tempByteBuffer.position() == tempByteBuffer.limit()) {
                        break;
                    }
                }

            }catch (IOException ioException) {
                logger.warn("zlib??????????????????",ioException);
                return null;
            }
        }
        //brotli???????????????????????????BroTil stream decoding failed?????????????????????????????????????????????????????????
        for (ByteBuffer brotliBytes :
                danMuBrotliCompressedList) {
            //????????????????????????
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
                logger.error("brotli???????????????IO?????????",ioException);
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
                    //??????????????????????????????????????????
                    if (tempByteBuffer.limit()-tempByteBuffer.position() < packetLength) {
                        break;
                    }
                    opsList.add(op);
                    byte[] packetBytes = new byte[packetLength];
                    decompressionDataStream.read(packetBytes, 0, packetLength);
                    String bodyString = new String(packetBytes, StandardCharsets.UTF_8);
                    danMuList.add(bodyString);
                    logger.debug("bodyString:{}",bodyString);
                    //???????????????
                    if (tempByteBuffer.position() == tempByteBuffer.limit()) {
                        break;
                    }
                }
            } catch (IOException ioException) {
                logger.warn("brotli??????????????????",ioException);
                return null;
            }
        }
        //?????????https://www.bilibili.com/read/cv14101053
        //https://www.lyyyuna.com/2016/03/14/bilibili-danmu01/
        //https://github.com/lovelyyoshino/Bilibili-Live-API/blob/master/API.WebSocket.md
        //https://www.cxyzjd.com/article/johnchang96/53192352
        //https://daidr.me/archives/code-526.html

        //???????????????https://github.com/BanqiJane/Bilibili_Danmuji/blob/7ca3d9f20f05c5f5ac2dbb46f422be4bfd6ca640/src/main/java/xyz/acproject/danmuji/thread/core/ParseMessageThread.java

        for (int i = 0; i < danMuList.size(); i++) {
            String body = danMuList.get(i);
            logger.debug("??????:{}",body);
            logger.debug("????????????:{}",opsList.get(i));
            if (opsList.get(i) == 5) {
                //jackson??????
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode jsonNode = objectMapper.readTree(body);
                    JsonNode cmd = jsonNode.get("cmd");
                    //???Null???????????????
                    if (cmd == null) {
                        logger.info("??????????????????{}", body);
                    } else {
                        String messageType = cmd.asText();
                        DanMuData danMuData = new DanMuData();
                        JsonNode info = jsonNode.get("info");
                        if (messageType.startsWith("DANMU_MSG")){
                            messageType = "DANMU_MSG";
                        }
                        switch (messageType) {
                            case "DANMU_MSG":
                                //????????????
                                danMuData.setMsgType(DanMuMessageType.DAN_MU.getText());
                                danMuData.setContent(info.get(1).asText());
                                //????????????
                                DanMuUserInfo danMuUserInfo = new DanMuUserInfo();
                                danMuUserInfo.setNickName(info.get(2).get(1).asText());
                                danMuData.setUserIfo(danMuUserInfo);
                                danMuData.setTimestamp(info.get(9).get("ts").asLong());
                                //????????????
                                DanMuFormat danMuFormat = new DanMuFormat();
                                String extra = info.get(0).get(15).get("extra").asText();
                                JsonNode extraJson = objectMapper.readTree(extra);
                                danMuFormat.setFontColor(extraJson.get("color").asInt());
                                danMuFormat.setFontSize(extraJson.get("font_size").asInt());
                                danMuData.setDanMuFormatData(danMuFormat);
                                logger.debug("danMuData:{}", danMuData);
                                danMuDataList.add(danMuData);
                                //?????????????????????????????????????????????break
                                //break;
                            default:
                                break;
                        }
                    }

                } catch (JsonProcessingException jsonParseException) {
                    logger.debug("error-index:{}",i);
                    logger.error("?????????????????????????????????:{}", body);
                }

            }
        }
        if (!danMuDataList.isEmpty()) {
            if (danMuDataList.size() == 1) {
                try {
                    danMuExportService.export(danMuDataList.get(0));
                } catch (IOException ioException) {
                    logger.error("????????????????????????IO?????????", ioException);
                }
            } else {
                danMuExportService.batchExport(danMuDataList);
            }
        }
        return danMuDataList;
    }
}
