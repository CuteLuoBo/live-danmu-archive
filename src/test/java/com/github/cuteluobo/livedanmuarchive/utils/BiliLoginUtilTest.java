package com.github.cuteluobo.livedanmuarchive.utils;

import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseUserInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class BiliLoginUtilTest {


    @Test
    @Timeout(10)
    void getUserBaseInfoByAppKey() throws URISyntaxException {
        String accessKey = "";
        String appKey = "4409e2ce8ffd12b8";
        String appSec = null;
        BaseUserInfo baseUserInfo = BiliLoginUtil.getUserBaseInfoByAppKey(accessKey, appKey, appSec);
        System.out.println(baseUserInfo);
    }

    @Test
    void checkLogin() {
        String ck = "";
        assertFalse(BiliLoginUtil.checkLogin(ck));
        String ck2 = System.getenv("BILI_COOKIE");
        assertTrue(BiliLoginUtil.checkLogin(ck2));
    }
}