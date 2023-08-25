package com.github.cuteluobo.livedanmuarchive.utils;

import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseUserInfo;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BiliLoginUtilTest {

    @Test
    void getUserBaseInfoByCk() {
    }

    @Test
    void getUserBaseInfoByAppKey() throws URISyntaxException {
        String accessKey = "";
        String appKey = "4409e2ce8ffd12b8";
        String appSec = null;
        BaseUserInfo baseUserInfo = BiliLoginUtil.getUserBaseInfoByAppKey(accessKey, appKey, appSec);
        System.out.println(baseUserInfo);
    }

}