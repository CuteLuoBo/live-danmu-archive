package com.github.cuteluobo.livedanmuarchive.utils;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Formatter;

/**
 * 格式操作相关工具类
 * @author CuteLuoBo
 * @date 2022/11/15 11:13
 */
public class FormatUtil {
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

    public static LocalDateTime millTime2localDataTime(@NotNull long millTime) {
        return LocalDateTime.ofEpochSecond(millTime / 1000, (int) (millTime % 1000), OffsetDateTime.now().getOffset());
    }

    /**
     * 替换特殊符号（BAS用）
     * @param text
     * @return
     */
    public static String replaceSymbol(@NotNull String text) {
        return text.replaceAll("\\\\", "\\\\\\\\");
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
}
