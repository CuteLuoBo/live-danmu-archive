package com.github.cuteluobo.livedanmuarchive.service.Impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class BiliVideoUpdateListenServiceImplTest {
    private static BiliVideoUpdateListenServiceImpl biliVideoUpdateListenService;

    @BeforeAll
    public static void setup() {
        biliVideoUpdateListenService = BiliVideoUpdateListenServiceImpl.getInstance();
        //从环境变量中获取cookie
        String cookie = System.getenv("BILI_COOKIE");
        biliVideoUpdateListenService.setCookie(cookie);
    }

    @Test
    void startVideoUpdateListen() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(8), () -> {
            biliVideoUpdateListenService.startVideoUpdateListen("1795867");
            Thread.sleep(5000);
        });
    }

    void addVideo() {
    }
}