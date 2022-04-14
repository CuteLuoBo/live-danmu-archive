package com.github.cuteluobo.livedanmuarchive.enums;

import java.util.Objects;

/**
 * 数据导出模式
 *
 * @author CuteLuoBo
 * @date 2022/4/4 15:28
 */
public enum ExportPattern {
    /**全部集中*/
    ALL_COLLECT("allCollect","文件集中在一个文件夹内",0),
    /**按每日文件夹分类*/
    DAY_FOLDER("dayFolder","文件按每日分文件夹存档",1)
    ;
    private String text;
    private String chineseText;
    private Integer typeValue;

    ExportPattern(String text, String chineseText, Integer typeValue) {
        this.text = text;
        this.chineseText = chineseText;
        this.typeValue = typeValue;
    }

    /**
     * 根据text获取枚举
     * @param text text
     * @return 枚举对象
     */
    public static ExportPattern getEnumByText(String text) {
        if (text != null) {
            for (ExportPattern danMuMessageType : ExportPattern.values()) {
                if (Objects.equals(danMuMessageType.getText(), text)) {
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
