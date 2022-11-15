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
}
