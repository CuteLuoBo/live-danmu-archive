package com.github.cuteluobo.livedanmuarchive.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    void StartSendTask() throws InterruptedException {
        danMuSenderController.createQueuePushTask().run();
        danMuSenderController.createStartSendTask().run();
        Thread.sleep(30000);
        danMuSenderController.stopTask();
        while (true) {

        }
    }
}