package com.github.cuteluobo.livedanmuarchive.model;

/**
 * @author CuteLuoBo
 * @date 2022/4/5 19:33
 */
public class DanMuDataModel{
    private Long id;
    private Integer userId;
    private String data;
    private Integer format;
    private Integer type;
    private Long createTime;
    private String createTimeText;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getFormat() {
        return format;
    }

    public void setFormat(Integer format) {
        this.format = format;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getCreateTimeText() {
        return createTimeText;
    }

    public void setCreateTimeText(String createTimeText) {
        this.createTimeText = createTimeText;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DanMuDataModel{");
        sb.append("id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", data='").append(data).append('\'');
        sb.append(", format=").append(format);
        sb.append(", type=").append(type);
        sb.append(", create_time=").append(createTime);
        sb.append(", createTimeString='").append(createTimeText).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
