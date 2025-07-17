package com.github.cuteluobo.livedanmuarchive.pojo.danmusender;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 弹幕发送状态统计
 *
 * @author: CuteLuoBo
 * @date: 2025/7/8  10:06
 * @version: 1.0.0
 */
public class SenderCount {
    private AtomicInteger success = new AtomicInteger();
    private AtomicInteger fail = new AtomicInteger();
    private AtomicInteger total = new AtomicInteger();

    @Override
    public String toString() {
        return "SenderCount{" +
                "success=" + success +
                ", fail=" + fail +
                ", total=" + total +
                '}';
    }

    public AtomicInteger getSuccess() {
        return success;
    }

    public void setSuccess(AtomicInteger success) {
        this.success = success;
    }

    public AtomicInteger getFail() {
        return fail;
    }

    public void setFail(AtomicInteger fail) {
        this.fail = fail;
    }

    public AtomicInteger getTotal() {
        return total;
    }

    public void setTotal(AtomicInteger total) {
        this.total = total;
    }
}
