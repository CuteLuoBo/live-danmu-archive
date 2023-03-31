package com.github.cuteluobo.livedanmuarchive.enums;


import java.util.Objects;

/**
 * @author CuteLuoBo
 * @date 2021/12/18 17:15
 */
public enum DanMuMessageType {
    /***/
    DAN_MU("danmu", "弹幕", 0), OTHER("other", "其他", 1),
    ;
    private String text;
    private String chineseText;
    private Integer typeValue;

    DanMuMessageType(String text, String chineseText, Integer typeValue) {
        this.text = text;
        this.chineseText = chineseText;
        this.typeValue = typeValue;
    }

    /**
     * 根据text获取枚举
     * @param text text
     * @return 枚举对象
     */
    public static DanMuMessageType getEnumByText(String text) {
        if (text != null) {
            for (DanMuMessageType danMuMessageType : DanMuMessageType.values()) {
                if (Objects.equals(danMuMessageType.getText(), text)) {
                    return danMuMessageType;
                }
            }
        }
        return null;
    }

    /**
     * 根据typeValue获取枚举
     *
     * @param typeValue 类型代号
     * @return 枚举对象
     */
    public static DanMuMessageType getEnumByTypeValue(Integer typeValue) {
        if (typeValue != null) {
            for (DanMuMessageType danMuMessageType : DanMuMessageType.values()) {
                if (Objects.equals(danMuMessageType.getTypeValue(), typeValue)) {
                    return danMuMessageType;
                }
            }
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public String getChineseText() {
        return chineseText;
    }

    public Integer getTypeValue() {
        return typeValue;
    }
}
