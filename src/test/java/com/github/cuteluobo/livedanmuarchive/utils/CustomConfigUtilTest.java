package com.github.cuteluobo.livedanmuarchive.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 配置工具类测试
 */
class CustomConfigUtilTest {
    CustomConfigUtil customConfigUtil = CustomConfigUtil.INSTANCE;


    @Test
    void showMapping() {
        System.out.println(customConfigUtil.getInitConfigMapping());
        ;
    }
}