package com.github.cuteluobo.livedanmuarchive.dto;

import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;

/**
 * 弹幕数据模型筛选类
 * @author CuteLuoBo
 * @date 2022/4/6 13:18
 */
public class DanMuDataModelSelector extends DanMuDataModel {

    public DanMuDataModelSelector() {}

    public DanMuDataModelSelector(Long startCreateTime) {
        this.startCreateTime = startCreateTime;
    }

    public DanMuDataModelSelector(Long startCreateTime, Long endCreateTime) {
        this.startCreateTime = startCreateTime;
        this.endCreateTime = endCreateTime;
    }

    /**
     * 创建时间起始
     */
    private Long startCreateTime;
    /**
     * 创建时间结尾
     */
    private Long endCreateTime;



    public Long getStartCreateTime() {
        return startCreateTime;
    }

    public void setStartCreateTime(Long startCreateTime) {
        this.startCreateTime = startCreateTime;
    }

    public Long getEndCreateTime() {
        return endCreateTime;
    }

    public void setEndCreateTime(Long endCreateTime) {
        this.endCreateTime = endCreateTime;
    }
}
