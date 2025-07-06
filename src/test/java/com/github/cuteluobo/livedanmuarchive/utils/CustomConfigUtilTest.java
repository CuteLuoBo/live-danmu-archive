package com.github.cuteluobo.livedanmuarchive.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 配置工具类测试
 */
class CustomConfigUtilTest {
    CustomConfigUtil customConfigUtil = CustomConfigUtil.INSTANCE;


    @Test
    @DisplayName("测试初始化配置")
    void showMapping() {
        assertNotNull(customConfigUtil.getInitConfigMapping());
    }
}