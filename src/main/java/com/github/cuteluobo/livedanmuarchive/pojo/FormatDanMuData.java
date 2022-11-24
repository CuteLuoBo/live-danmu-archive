package com.github.cuteluobo.livedanmuarchive.pojo;

/**
 * 格式化导出的弹幕数据
 * @author CuteLuoBo
 * @date 2022/11/24 15:08
 */
public class FormatDanMuData {
    private long startTime;
    private String content;
    private float fontSize;

    public FormatDanMuData(long startTime, String content, float fontSize) {
        this.startTime = startTime;
        this.content = content;
        this.fontSize = fontSize;
    }

    public FormatDanMuData() {

    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }
}
