package com.github.cuteluobo.livedanmuarchive.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseResult;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoAllInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoPage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BiliDanMuUtilTest {

    @Test
    @DisplayName("测试弹幕发送")
//    @Disabled
    void sendDanMu() throws URISyntaxException, IOException, InterruptedException {
        String testBV = "BV1f64y1i7fk";
        String cookie = null;
        String accessKey = "";
        BaseResult<VideoAllInfo> baseResult = BiliVideoUtil.getVideoAllInfo(testBV,null,null);
        VideoAllInfo videoAllInfo = baseResult.getData();
        List<VideoPage> videoPageList = videoAllInfo.getPages();
        assertNotNull(videoPageList);
        VideoPage videoPage = videoPageList.get(0);
        long cid = videoPage.getCid();
        HttpResponse<String> stringHttpResponse = BiliDanMuUtil.sendDanMu(cid, "嘟嘟噜", testBV, 0, 0L, null, 25.0f, 0, 1, cookie, accessKey);
        String bodyString = stringHttpResponse.body();
        System.out.println(bodyString);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode body = objectMapper.readTree(bodyString);
    }
}