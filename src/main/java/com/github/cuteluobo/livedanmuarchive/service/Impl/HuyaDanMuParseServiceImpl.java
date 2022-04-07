package com.github.cuteluobo.livedanmuarchive.service.Impl;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuMessageType;
import com.github.cuteluobo.livedanmuarchive.pojo.*;
import com.github.cuteluobo.livedanmuarchive.service.DanMuExportService;
import com.github.cuteluobo.livedanmuarchive.service.DanMuParseService;
import com.qq.tars.protocol.tars.TarsInputStream;
import com.qq.tars.protocol.tars.TarsStructBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 虎牙弹幕信息解析猎
 * @author CuteLuoBo
 * @date 2022/2/11 12:04
 */
public class HuyaDanMuParseServiceImpl  implements DanMuParseService {
    private DanMuExportService danMuExportService;
    private HuyaDanMuUserDataTarsBase huyaDanMuUserDataTarsBase;
    private HuyaDanMuFormatDataTarsBase huyaDanMuFormatDataTarsBase;

    Logger logger = LoggerFactory.getLogger(HuyaDanMuParseServiceImpl.class);

    private static final long normalDanMuMessageType = 1400L;

    public HuyaDanMuParseServiceImpl(DanMuExportService danMuExportService) {
        this.danMuExportService = danMuExportService;
        huyaDanMuUserDataTarsBase = new HuyaDanMuUserDataTarsBase();
        huyaDanMuFormatDataTarsBase = new HuyaDanMuFormatDataTarsBase();
    }

    /**
     * 解析返回的字符串类型信息
     *
     * @param Message 字符串信息
     * @return 处理完成的弹幕信息
     */
    @Override
    public DanMuData parseMessage(String Message) {
        //暂不实现字符串解析模式
        return null;
    }

    /**
     * 解析返回的字节流类型信息
     *
     * @param byteBufferMessage
     * @return 处理完成的弹幕信息
     */
    @Override
    public DanMuData parseMessage(ByteBuffer byteBufferMessage) throws IOException {
        DanMuData danMuData = new HuyaDanMuData();
        TarsInputStream tarsInputStream = new TarsInputStream(byteBufferMessage);
        //TODO 解决部分直播弹幕用户无法记录的问题（完全没有收到任何消息,可能是API原因）
        int messageValue1 = tarsInputStream.read(0, 0, false);
        logger.debug("messageValue1读取值{}",messageValue1);
        if (messageValue1 == 7) {
            //此处传byte[0]，表示让返回值返回为byte[]类型，实际输出与传入数组无关
            byte[] tempArray = new byte[0];
            tarsInputStream = new TarsInputStream(tarsInputStream.read(tempArray, 1, false));

            //real-url中本身是传Int64类型
            long messageTypeValue = tarsInputStream.read(0L, 1, false);
            logger.info("tarsInputStream读取值{}",messageTypeValue);
            //1001=贵族续费广播,1400=弹幕消息，8006=(贵族)进房,6501=礼物,6502=全服礼物

            if (messageTypeValue == normalDanMuMessageType) {

                tarsInputStream = new TarsInputStream(tarsInputStream.read(tempArray, 2, false));
                //默认为GBK，需转为utf-8
                tarsInputStream.setServerEncoding("utf-8");

                //用户信息
                TarsStructBase tempBase = tarsInputStream.read(huyaDanMuUserDataTarsBase, 0, false);
                if (tempBase != null) {
                    HuyaDanMuUserDataTarsBase dataBase = (HuyaDanMuUserDataTarsBase) tempBase;
                    danMuData.setUserIfo(dataBase.getHuYaUserInfo());
                }

                //内容
                danMuData.setContent(tarsInputStream.read("", 3, false));
                //设置获取时间
                danMuData.setTimestamp(System.currentTimeMillis());

                //弹幕格式
                TarsStructBase tempBase2 = tarsInputStream.read(huyaDanMuFormatDataTarsBase, 6, false);
                if (tempBase2 != null) {
                    HuyaDanMuFormatDataTarsBase dataBase = (HuyaDanMuFormatDataTarsBase) tempBase2;
                    danMuData.setDanMuFormatData(dataBase.getDanMuFormatData());
                }
            }
        }
        //设置收到的消息类型，有用户信息时为弹幕，进行导出(后续增加自定义功能)
        if (danMuData.getUserIfo()!=null ) {
            danMuData.setMsgType(DanMuMessageType.DAN_MU.getText());
            //导出操作
            danMuExportService.export(danMuData);
        } else {
            danMuData.setMsgType(DanMuMessageType.OTHER.getText());
        }
        logger.info("解析消息:{}",danMuData);


        return danMuData;
    }
}
