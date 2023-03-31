package com.github.cuteluobo.livedanmuarchive.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FormatUtilTest {

    @Test
    void millTime2String() {
        assertEquals(FormatUtil.millTime2String(0), "0:00:00.000");
        assertEquals(FormatUtil.millTime2String(60 * 1000), "0:01:00.000");
        assertEquals(FormatUtil.millTime2String(60 * 60 * 1000), "1:00:00.000");
    }

    @Test
    void videoTimeString2MillTime() {
        assertEquals(0, FormatUtil.videoTimeString2MillTime("00:00.000"));
        assertEquals(0, FormatUtil.videoTimeString2MillTime("0:00:00.000"));
        assertEquals(60 * 60 * 1000, FormatUtil.videoTimeString2MillTime("1:00:00.000"));
        assertEquals(60 * 60 * 1000 + 1, FormatUtil.videoTimeString2MillTime("1:00:00.001"));
        assertEquals(60 * 1000, FormatUtil.videoTimeString2MillTime("01:00.000"));
        assertEquals(60 * 1000+1, FormatUtil.videoTimeString2MillTime("01:00.001"));
    }

    public void test2() {
        //测试Stream流的null过滤
        List<String> testList = Arrays.asList("ok","fail","ok");
        List<String> result = testList.stream().map(t -> t.equals("ok") ? "1" : null).filter(Objects::nonNull).collect(Collectors.toList());
        result.forEach(System.out::println);
    }
}