package com.github.cuteluobo.livedanmuarchive.pojo;

import java.util.Objects;

/**
 * @author CuteLuoBo
 * @date 2022/2/12 21:09
 */
public class DanMuFormat {
    /**
     * 字体颜色
     */
    private int fontColor = 1;

    /**
     * 字体大小
     */
    private int fontSize = 4;
    /**
     * 文本速度
     */
    private int textSpeed = 0;
    /**
     * 过渡类型
     */
    private int transitionType = 1;
    /**
     * 是否弹出弹幕
     */
    private int popupStyle = 0;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DanMuFormatData{");
        sb.append("fontColor=").append(fontColor);
        sb.append(", fontSize=").append(fontSize);
        sb.append(", textSpeed=").append(textSpeed);
        sb.append(", transitionType=").append(transitionType);
        sb.append(", popupStyle=").append(popupStyle);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DanMuFormat)) {
            return false;
        }
        DanMuFormat that = (DanMuFormat) o;
        return getFontColor() == that.getFontColor() && getFontSize() == that.getFontSize() && getTextSpeed() == that.getTextSpeed() && getTransitionType() == that.getTransitionType() && getPopupStyle() == that.getPopupStyle();
    }



    @Override
    public int hashCode() {
        return Objects.hash(getFontColor(), getFontSize(), getTextSpeed(), getTransitionType(), getPopupStyle());
    }

    public int getFontColor() {
        return fontColor;
    }

    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getTextSpeed() {
        return textSpeed;
    }

    public void setTextSpeed(int textSpeed) {
        this.textSpeed = textSpeed;
    }

    public int getTransitionType() {
        return transitionType;
    }

    public void setTransitionType(int transitionType) {
        this.transitionType = transitionType;
    }

    public int getPopupStyle() {
        return popupStyle;
    }

    public void setPopupStyle(int popupStyle) {
        this.popupStyle = popupStyle;
    }
}
