package com.github.cuteluobo.livedanmuarchive.pojo;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 弹幕发送结果统计数据
 *
 * @author CuteLuoBo
 * @date 2023/3/10 15:07
 */
public class DanMuSenderResult<T> {
    private AtomicLong total = new AtomicLong();
    private AtomicLong successNum= new AtomicLong();
    private AtomicLong failNum= new AtomicLong();
    private long startTime = System.currentTimeMillis();

    /**
     * 最后工作的视频分P索引
     */
    private int lastWorkVideoPartIndex;
    /**
     * 最后工作的弹幕分页数
     */
    private int lastWorkDataPageNum;


    /**
     * 中断时剩余的数据
     */
    private List<DanMuData> residueDataList;

    private T processedVideoData;
    @Override
    public DanMuSenderResult<T> clone(){
        DanMuSenderResult<T> clone = new DanMuSenderResult<>();
        AtomicLong total1 = new AtomicLong();
        total1.set(total.get());
        clone.setTotal(total1);

        AtomicLong successNum1= new AtomicLong();
        successNum1.set(successNum.get());
        clone.setSuccessNum(successNum1);

        AtomicLong failNum1= new AtomicLong();
        failNum1.set(failNum.get());
        clone.setFailNum(failNum1);

        long startTime = System.currentTimeMillis();
        clone.setStartTime(startTime);

        clone.setResidueDataList(residueDataList);
        clone.setLastWorkDataPageNum(lastWorkDataPageNum);
        clone.setLastWorkVideoPartIndex(lastWorkVideoPartIndex);
        clone.setProcessedVideoData(processedVideoData);
        return clone;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DanMuSenderResult{");
        sb.append("total=").append(total);
        sb.append(", successNum=").append(successNum);
        sb.append(", failNum=").append(failNum);
        sb.append(", startTime=").append(startTime);
        sb.append(", lastWorkVideoPartIndex=").append(lastWorkVideoPartIndex);
        sb.append(", lastWorkDataPageNum=").append(lastWorkDataPageNum);
        sb.append(", residueDataList=").append(residueDataList);
        sb.append(", processedVideoData=").append(processedVideoData);
        sb.append('}');
        return sb.toString();
    }

    public long success() {
        total.incrementAndGet();
        return successNum.incrementAndGet();
    }

    public long fail() {
        total.incrementAndGet();
        return failNum.incrementAndGet();
    }

    public AtomicLong getTotal() {
        return total;
    }

    public void setTotal(AtomicLong total) {
        this.total = total;
    }

    public AtomicLong getSuccessNum() {
        return successNum;
    }

    public void setSuccessNum(AtomicLong successNum) {
        this.successNum = successNum;
    }

    public AtomicLong getFailNum() {
        return failNum;
    }

    public void setFailNum(AtomicLong failNum) {
        this.failNum = failNum;
    }

    public List<DanMuData> getResidueDataList() {
        return residueDataList;
    }

    public void setResidueDataList(List<DanMuData> residueDataList) {
        this.residueDataList = residueDataList;
    }

    public int getLastWorkVideoPartIndex() {
        return lastWorkVideoPartIndex;
    }

    public void setLastWorkVideoPartIndex(int lastWorkVideoPartNum) {
        this.lastWorkVideoPartIndex = lastWorkVideoPartNum;
    }

    public int getLastWorkDataPageNum() {
        return lastWorkDataPageNum;
    }

    public void setLastWorkDataPageNum(int lastWorkDataPageNum) {
        this.lastWorkDataPageNum = lastWorkDataPageNum;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public T getProcessedVideoData() {
        return processedVideoData;
    }

    public void setProcessedVideoData(T processedVideoData) {
        this.processedVideoData = processedVideoData;
    }
}
