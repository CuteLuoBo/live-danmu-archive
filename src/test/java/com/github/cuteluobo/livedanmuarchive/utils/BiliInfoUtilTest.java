package com.github.cuteluobo.livedanmuarchive.utils;

import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.DynamicVideoData;
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
        DynamicVideoData dynamicVideoData = BiliInfoUtil.getDynamicVideoList(uid, offset);
        assertNotNull(dynamicVideoData);
        List<Map.Entry<String,Long>> bvList = dynamicVideoData.getVideoList();
        System.out.println("总数:" + bvList.size());
        bvList.forEach(System.out::println);
        System.out.println("动态偏移ID："+dynamicVideoData.getOffsetId());
    }
}