package com.github.cuteluobo.livedanmuarchive.pojo;

import java.util.List;

/**
 * 分页数据类
 * @author CuteLuoBo
 * @date 2022/4/6 12:16
 */
public class DataPage<T> {
    private List<T> data;
    /**
     * 当前页数
     */
    private Integer current;
    /**
     * 单页数量
     */
    private Integer pageSize;
    /**
     * 总结果数量
     */
    private Integer total;
    /**
     * 最大页数
     */
    private Integer maxPageNum;


    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }


    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getMaxPageNum() {
        return maxPageNum;
    }

    public void setMaxPageNum(Integer maxPageNum) {
        this.maxPageNum = maxPageNum;
    }
}
