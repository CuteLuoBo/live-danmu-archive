package com.github.cuteluobo.livedanmuarchive.utils;

import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuUserInfo;
import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 格式操作相关工具类
 * @author CuteLuoBo
 * @date 2022/11/15 11:13
 */
public class FormatUtil {

    /**
     * 对post的map进行编码转为string
     * @param map 数据键值对
     * @return 转换完成的string
     */
    public static String encodePostStringByMap(Map<String, Object> map) {
        return map.entrySet().stream().map(entry ->
                String.join("="
                        , URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                        , URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8)
        )).collect(Collectors.joining("&"));
    }
    /**
     * 毫秒时间转为视频时间格式
     * 0 -> 0:00:00.000
     * @param time 毫秒时间
     * @return 视频时间字符串
     */
    public static String millTime2String(long time) {
        Formatter formatter = new Formatter();
        long millSeconds = time % 1000;
        long totalSeconds = time / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        return formatter.format("%d:%02d:%02d.%03d", hours, minutes, seconds, millSeconds).toString();
    }

    /**
     * 视频时间格式字符串传为毫秒时间
     * 0:00:00.000 -> 0
     * @param timeString  时间字符串
     * @return 毫秒时间
     */
    public static long videoTimeString2MillTime(@NotNull String timeString) {
        long time = 0;
        String[] split = timeString.split(":");
        for (int i = split.length - 1; i >= 0; i--) {
            String input = split[i];
            //毫秒，分割最后的.
            int dotIndex = input.lastIndexOf(".");
            if (dotIndex != -1) {
                String mill = input.substring(dotIndex + 1);
                time += Long.parseLong(mill);
                input = input.substring(0, dotIndex);
            }
            time += Long.parseLong(input) * Math.pow(60, Math.abs(split.length - 1 - i)) * 1000;
        }
        return time;
    }

    public static long localDataTime2MillTime(@NotNull LocalDateTime dateTime) {
        return dateTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
    }

    public static LocalDateTime millTime2localDataTime(long millTime) {
        return LocalDateTime.ofEpochSecond(millTime / 1000, (int) (millTime % 1000), OffsetDateTime.now().getOffset());
    }

    /**
     * 替换特殊符号（BAS用）
     * @param text 待替换文本
     * @return 替换完成后的文本
     */
    public static String replaceSymbol(@NotNull String text) {
        return text.replaceAll("\\\\", "\\\\\\\\").replaceAll("\r"," ").replaceAll("\n"," ").replaceAll("\t"," ");
    }

    /**
     * int到高位字节数组转换
     * @param v 待转换数据
     * @return 转换结果
     */
    public static byte[] int2HighBytes(int v) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((v >>> 0) & 0xFF);
        bytes[1] = (byte) ((v >>> 8) & 0xFF);
        bytes[2] = (byte) ((v >>> 16) & 0xFF);
        bytes[3] = (byte) ((v >>> 24) & 0xFF);
        return bytes;
    }

    /**
     * 高位字节数组转int
     * @param bytes  字节数组
     * @return 转换的int
     */
    public static int highBytes2Int(byte[] bytes) {
        //https://blog.csdn.net/m0_61849361/article/details/125267156
        return (bytes[0] & 0xff)
                |((bytes[1] & 0xff) << 8)
                |((bytes[2] & 0xff) << 16)
                |((bytes[3] & 0xff) << 24);
    }

    /**
     * 合并相似信息（单符号意义）
     * @param danMuDataList 弹幕列表
     * @return 处理的数量
     */
    public static int mergeSimilarMessage(List<DanMuData> danMuDataList){
        int total = 0;
        Map<String, DanMuData> tempMap = new HashMap<>(danMuDataList.size());
        Map<String, Integer> totalMap = new HashMap<>(danMuDataList.size()/2);
        //遍历
        Iterator<DanMuData> danMuDataIterator = danMuDataList.iterator();
        while (danMuDataIterator.hasNext()) {
            DanMuData danMuData = danMuDataIterator.next();
            //移除列表数据
            danMuDataIterator.remove();
            String message = danMuData.getContent();
            //替换中英文字符
            message = replacePunctuation(message);
            danMuData.setContent(message);
            DanMuData temp = tempMap.get(message);
            //无记录时，即未重复
            if (temp == null) {
                tempMap.put(message, danMuData);
            } else {
                //获取当前统计信息
                Integer messageTotal = totalMap.get(message);
                //有记录时+1
                if (messageTotal != null) {
                    totalMap.put(message, messageTotal + 1);
                }
                //否则为新重复项
                else {
                    //设置为系统操作后放回
                    danMuData.setUserIfo(DanMuUserInfo.SYSTEM);
                    tempMap.put(message, danMuData);
                    totalMap.put(message, 2);
                }
                total++;
            }
        }
        totalMap.forEach((key, value) -> {
            DanMuData danMuData = tempMap.get(key);
            danMuData.setContent(danMuData.getContent() + " x " + value);
        });
        danMuDataList.addAll(tempMap.values());
        //释放空间
        tempMap.clear();
        totalMap.clear();
        return total;
    }

    public static String replacePunctuation(String string) {
        string = string.trim();
        string = string.replaceAll("？", "?");
        string = string.replaceAll("！", "!");
        return string;
    }
}
