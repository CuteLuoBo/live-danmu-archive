package com.github.cuteluobo.livedanmuarchive.async;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BiliVideoUpdateTaskTest {

    @Test
    @DisplayName("手动测试更新视频ID")
    @Disabled
    void updateLatestVideoId() {
        //从环境变量中获取cookie
        String cookie = System.getenv("BILI_COOKIE");
        BiliVideoUpdateTask biliVideoUpdateTask = new BiliVideoUpdateTask("1795867",cookie);
        assertTimeoutPreemptively(Duration.of(5, TimeUnit.SECONDS.toChronoUnit()), () -> {
            biliVideoUpdateTask.updateLatestVideoId().run();
            Thread.sleep(3 * 1000L);
        });
    }
}