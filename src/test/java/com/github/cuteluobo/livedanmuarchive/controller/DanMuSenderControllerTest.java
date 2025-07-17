package com.github.cuteluobo.livedanmuarchive.controller;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

class DanMuSenderControllerTest {
    private static DanMuSenderController danMuSenderController;
    @BeforeAll
    public static void setup() {
        danMuSenderController = DanMuSenderController.getInstance();
    }

    @Test
    @Disabled
    void QueuePushTask() throws InterruptedException {
        danMuSenderController.createQueuePushTask().run();
        while (true){
            TimeUnit.SECONDS.sleep(60);
        }
    }

    @Test
    @DisplayName("手动测试发送列表任务")
    @Disabled
    void StartSendTask() throws InterruptedException {
        danMuSenderController.createQueuePushTask().run();
        danMuSenderController.createStartSendTask().run();
        while (true){
            TimeUnit.SECONDS.sleep(60);
        }
//        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(8), () -> {
//            Thread.sleep(30000);
//            danMuSenderController.stopTask();
//            Thread.sleep(30000);
//        });
    }
}