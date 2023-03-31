package com.github.cuteluobo.livedanmuarchive.service.Impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BiliVideoUpdateListenServiceImplTest {
    private static BiliVideoUpdateListenServiceImpl biliVideoUpdateListenService;

    @BeforeAll
    public static void setup() {
        biliVideoUpdateListenService = BiliVideoUpdateListenServiceImpl.getInstance();
    }

    @Test
    void startVideoUpdateListen() {
        biliVideoUpdateListenService.startVideoUpdateListen("1795867");
        while (true) {

        }
    }

    @Test
    void addVideo() {
    }
}