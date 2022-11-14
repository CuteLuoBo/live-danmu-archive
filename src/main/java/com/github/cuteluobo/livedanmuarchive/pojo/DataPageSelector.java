package com.github.cuteluobo.livedanmuarchive.pojo;

/**
 * 分页数据条件
 * @author CuteLuoBo
 * @date 2022/4/6 13:23
 */
public class DataPageSelector {
    private int current;
    private int pageSize;

    public DataPageSelector() {
    }

    public DataPageSelector(int current, int pageSize) {
        this.current = current;
        this.pageSize = pageSize;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
