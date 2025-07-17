package com.github.cuteluobo.livedanmuarchive.async;

/**
 * 待重试的任务
 *
 * @author: CuteLuoBo
 * @date: 2025/7/8  10:34
 * @version: 1.0.0
 */
public class RetryTask<T> {
    private T task;
    private int retryCount;

    public RetryTask(T task, int retryCount) {
        this.task = task;
        this.retryCount = retryCount;
    }

    /**
     * 减去重试次数
     * @return 是否还有重试次数
     */
    public boolean decreaseRetryCount() {
        retryCount--;
        return retryCount >= 0;
    }

    public T getTask() {
        return task;
    }

    public void setTask(T task) {
        this.task = task;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
