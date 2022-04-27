package com.github.cuteluobo.livedanmuarchive.service;

import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * 弹幕解析接口
 * @author CuteLuoBo
 * @date 2022/2/11 11:56
 */
public interface DanMuParseService {

    /**
     * 解析返回的字符串类型信息
     * @param Message 字符串信息
     * @return 处理完成的弹幕信息
     */
    List<DanMuData> parseMessage(String Message);

    /**
     * 解析返回的字节流类型信息
     * @param byteBufferMessage
     * @return 处理完成的弹幕信息
     */
    List<DanMuData> parseMessage(ByteBuffer byteBufferMessage) throws ServiceException;

}
