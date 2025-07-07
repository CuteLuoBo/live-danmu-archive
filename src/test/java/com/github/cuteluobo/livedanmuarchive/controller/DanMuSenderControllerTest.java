package com.github.cuteluobo.livedanmuarchive.controller;

import org.junit.jupiter.api.*;

import java.time.Duration;

class DanMuSenderControllerTest {
    private static DanMuSenderController danMuSenderController;
    @BeforeAll
    public static void setup() {
        danMuSenderController = DanMuSenderController.getInstance();
    }

    @Test
    @Disabled
    void QueuePushTask() {
        danMuSenderController.createQueuePushTask().run();
    }

    @Test
    @DisplayName("手动测试发送列表任务")
    @Disabled
    void StartSendTask() {
        danMuSenderController.createQueuePushTask().run();
        danMuSenderController.createStartSendTask().run();
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(8), () -> {
            Thread.sleep(30000);
            danMuSenderController.stopTask();
            Thread.sleep(30000);
        });
    }
}