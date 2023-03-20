package com.github.cuteluobo.livedanmuarchive.dto;

import com.github.cuteluobo.livedanmuarchive.model.DanmuAccountTaskModel;

/**
 * 弹幕发送账户任务筛选类
 * @author CuteLuoBo
 * @date 2023/3/18 10:54
 */
public class DanMuAccountTaskSelector extends DanmuAccountTaskModel {

    /**
     * 创建时间起始
     */
    private Long startCreateTime;
    /**
     * 创建时间结尾
     */
    private Long endCreateTime;


    /**
     * 更新时间起始
     */
    private Long startUpdateTime;
    /**
     * 更新时间起始
     */
    private Long endUpdateTime;

    /**
     * 完成时间起始
     */
    private Long startFinishTime;
    /**
     * 完成时间起始
     */
    private Long endFinishTime;

    private Integer pageIndex;

    private Integer pageSize;

    @Override
    public Integer getPageIndex() {
        return pageIndex;
    }

    @Override
    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    @Override
    public Integer getPageSize() {
        return pageSize;
    }

    @Override
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getStartUpdateTime() {
        return startUpdateTime;
    }

    public void setStartUpdateTime(Long startUpdateTime) {
        this.startUpdateTime = startUpdateTime;
    }

    public Long getEndUpdateTime() {
        return endUpdateTime;
    }

    public void setEndUpdateTime(Long endUpdateTime) {
        this.endUpdateTime = endUpdateTime;
    }

    public Long getStartFinishTime() {
        return startFinishTime;
    }

    public void setStartFinishTime(Long startFinishTime) {
        this.startFinishTime = startFinishTime;
    }

    public Long getEndFinishTime() {
        return endFinishTime;
    }

    public void setEndFinishTime(Long endFinishTime) {
        this.endFinishTime = endFinishTime;
    }

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
