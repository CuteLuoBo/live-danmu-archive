package com.github.cuteluobo.livedanmuarchive.async;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BiliVideoUpdateTaskTest {

    @Test
    void updateLatestVideoId() {
        BiliVideoUpdateTask biliVideoUpdateTask = new BiliVideoUpdateTask("1795867");
        biliVideoUpdateTask.updateLatestVideoId().run();
        while (true) {

        }
    }
}