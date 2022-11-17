package com.github.cuteluobo.livedanmuarchive.command.base;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程命名
 * @author CuteLuoBo
 * @date 2022/9/17 17:21
 */
public class MyThreadFactory implements ThreadFactory {

    /**
     * 线程基本名称
     */
    private final String prefix;

    private final AtomicInteger nextId = new AtomicInteger();

    public MyThreadFactory(String prefix) {
        this.prefix = prefix;
    }


    /**
     * 创建之定义命名后的新线程
     * @param r 执行任务
     * @return 创建后的线程
     */
    @Override
    public Thread newThread(@NotNull Runnable r) {
        return new Thread(r, prefix + '-' + nextId.incrementAndGet());
    }
}