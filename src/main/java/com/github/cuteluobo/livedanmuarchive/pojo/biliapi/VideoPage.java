package com.github.cuteluobo.livedanmuarchive.pojo.biliapi;

/**
 * B站视频分P对象
 * @author CuteLuoBo
 * @date 2022/12/28 12:36
 */
public class VideoPage {
    /**
     * 分P CID
     */
    private long cid;
    /**
     * 当前分P（索引）
     */
    private int page;
    /**
     * 视频来源
     * vupload：普通上传（B站）
     * hunan：芒果TV
     * qq：腾讯
     */
    private String from;
    /**
     * 分P名称
     */
    private String partName;
    /**
     * 分P持续时间(s)
     */
    private long duration;
    /**
     * 视频宽度（部分视频可能无法解析）
     */
    private int width = 0;
    /**
     * 视频高度（部分视频可能无法解析）
     */
    private int height = 0;
    /**
     * 宽高是否调换
     */
    private boolean rotate = false;

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }
}
