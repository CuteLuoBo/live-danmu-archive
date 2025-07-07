package com.github.cuteluobo.livedanmuarchive.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseResult;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoAllInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BiliDanMuUtilTest {

//    @Test
    @DisplayName("测试弹幕发送")
//    @Disabled
    void sendDanMu() throws URISyntaxException, IOException, InterruptedException {
        String testBV = "BV1f64y1i7fk";
        String cookie = System.getenv("BILI_COOKIE");
        String accessKey = "";
        //解析稿件分片信息
        BaseResult<VideoAllInfo> baseResult = BiliVideoUtil.getVideoAllInfo(testBV,null,null);
        VideoAllInfo videoAllInfo = baseResult.getData();
        List<VideoPage> videoPageList = videoAllInfo.getPages();
        assertNotNull(videoPageList);
        VideoPage videoPage = videoPageList.get(0);
        long cid = videoPage.getCid();
        //尝试发送
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            HttpResponse<String> stringHttpResponse = BiliDanMuUtil.sendDanMu(cid, "嘟嘟噜", testBV, 0, 0L, null, 25.0f, 0, 1, cookie, accessKey);
            String bodyString = stringHttpResponse.body();
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