package com.github.cuteluobo.livedanmuarchive.utils;

import org.junit.jupiter.api.Test;

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
}