package com.github.cuteluobo.livedanmuarchive.pojo;

/**
 * 弹幕导出数据信息
 *
 * @author CuteLuoBo
 * @date 2022/11/16 13:00
 */
public class DanMuExportDataInfo<T> {
    /**
     * 数据
     */
    private T data;
    /**
     * 总计弹幕数量
     */
    private long totalNum;
    /**
     * 实际使用的弹幕数量
     */
    private long usageNum;

    public DanMuExportDataInfo() {
    }

    public DanMuExportDataInfo(T data, long totalNum, long usageNum) {
        this.data = data;
        this.totalNum = totalNum;
        this.usageNum = usageNum;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(long totalNum) {
        this.totalNum = totalNum;
    }

    public long getUsageNum() {
        return usageNum;
    }

    public void setUsageNum(long usageNum) {
        this.usageNum = usageNum;
    }
}
