package com.github.cuteluobo.livedanmuarchive.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BiliInfoUtilTest {

    @Test
    @DisplayName("测试获取用户动态列表视频数据")
    void getDynamicVideoList() throws URISyntaxException, IOException, InterruptedException {
        long uid = 1795867;
        long offset = 0;
        List<String> bvList = BiliInfoUtil.getDynamicVideoList(uid, offset);
        assertNotNull(bvList);
        System.out.println("总数:" + bvList.size());
        bvList.forEach(System.out::println);
    }
}