package com.github.cuteluobo.livedanmuarchive.utils;

import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BiliDanMuAuthInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.DynamicVideoData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BiliInfoUtilTest {

    @Test
    @DisplayName("测试获取用户动态列表视频数据")
    void getDynamicVideoList() throws URISyntaxException, IOException, InterruptedException, ServiceException {
        long uid = 1795867;
        long offset = 0;
        //从环境变量中获取cookie
        String cookie = System.getenv("BILI_COOKIE");
        DynamicVideoData dynamicVideoData = BiliInfoUtil.getDynamicVideoList(uid, offset, cookie);
        assertNotNull(dynamicVideoData);
        List<Map.Entry<String, Long>> bvList = dynamicVideoData.getVideoList();
        assertFalse(bvList.isEmpty());
        System.out.println("总数:" + bvList.size());
        bvList.forEach(System.out::println);
        System.out.println("动态偏移ID：" + dynamicVideoData.getOffsetId());
    }

    @Test
    @DisplayName("请求弹幕流认证包")
    public void giveAuthInfoWhenRequestReturnNotNull() throws Exception {
        //从环境变量中获取cookie
        String cookie = System.getenv("BILI_COOKIE");
        BiliDanMuAuthInfo biliDanMuAuthInfo = BiliInfoUtil.getAuthInfo(7734200, BiliLoginUtil.getUserBaseInfoByCk(cookie));
        Assertions.assertNotNull(biliDanMuAuthInfo);
        System.out.println(biliDanMuAuthInfo);
        Assertions.assertNotNull(biliDanMuAuthInfo.getHostList());
        Assertions.assertNotNull(biliDanMuAuthInfo.getToken());
    }
}