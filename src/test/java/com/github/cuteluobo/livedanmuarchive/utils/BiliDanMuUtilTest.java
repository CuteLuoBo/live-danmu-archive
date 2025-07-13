package com.github.cuteluobo.livedanmuarchive.utils;

import cn.hutool.core.date.TimeInterval;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseResult;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseUserInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoAllInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoPage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BiliDanMuUtilTest {
    String testBV,cookie,accessKey,msg;
    Long cid;
    BaseUserInfo baseUserInfo;
    @BeforeEach
    void setUp() throws URISyntaxException, IOException, InterruptedException {
        testBV = "BV1f64y1i7fk";
        cookie = System.getenv("BILI_COOKIE");
        accessKey = "";
        msg = "rua1";
        //解析稿件分片信息
        BaseResult<VideoAllInfo> baseResult = BiliVideoUtil.getVideoAllInfo(testBV,null,null);
        VideoAllInfo videoAllInfo = baseResult.getData();
        List<VideoPage> videoPageList = videoAllInfo.getPages();
        assertNotNull(videoPageList);
        VideoPage videoPage = videoPageList.get(0);
        cid = videoPage.getCid();
        baseUserInfo = BiliLoginUtil.getUserBaseInfoByCk(cookie);
    }

    @Test
    @DisplayName("测试弹幕发送")
//    @Disabled
    void sendDanMu() throws URISyntaxException, IOException, InterruptedException {
        //尝试发送
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            TimeInterval interval = new TimeInterval();
            HttpResponse<String> stringHttpResponse = BiliDanMuUtil.sendDanMu(cid, msg, testBV, 0, 0L, null, 25.0f, 0, 1, baseUserInfo.getImgKey(), baseUserInfo.getSubKey(), cookie, accessKey);
            String bodyString = stringHttpResponse.body();
            System.out.println("请求耗时："+interval.intervalPretty());
            //输出返回消息
            System.out.println(bodyString);
            //解析JSON并验证
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode body = objectMapper.readTree(bodyString);
            int code = body.get("code").asInt();
            //允许成功发送/账户未登陆
            assertTrue(code == 0 || code == -101);
        });
    }

    @Test
    @DisplayName("测试Netty客户端发送")
//    @Disabled
    void sendDanMuNetty() throws URISyntaxException, IOException, InterruptedException {
        //尝试发送
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            TimeInterval interval = new TimeInterval();
            interval.start();
            String bodyString = BiliDanMuUtil.sendDanMuByNetty(cid, msg, testBV, 0, 0L, null, 25.0f, 0, 1, baseUserInfo.getImgKey(), baseUserInfo.getSubKey(), cookie, accessKey);
            System.out.println("请求耗时："+interval.intervalPretty());
            //输出返回消息
            System.out.println(bodyString);
            //解析JSON并验证
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode body = objectMapper.readTree(bodyString);
            int code = body.get("code").asInt();
            //允许成功发送/账户未登陆
            assertTrue(code == 0 || code == -101);
        });
    }


}