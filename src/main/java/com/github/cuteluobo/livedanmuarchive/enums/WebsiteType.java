package com.github.cuteluobo.livedanmuarchive.enums;


import java.util.Objects;

/**
 * 网站类型
 *
 * @author CuteLuoBo
 * @date 2021/12/16 16:43
 */
public enum WebsiteType {
    /**枚举类型*/
    Huya("虎牙", "huya"),Bil("哔哩哔哩","bilibili"),Douyu("斗鱼","douyu")
    ;
    /**
     * 名称
     */
    private String name;
    /**缩写文本*/
    private String text;

    WebsiteType(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public static WebsiteType getEnumByValue(String text) {
        if (text != null) {
            for (WebsiteType em : WebsiteType.values()) {
                if (Objects.equals(em.getText(), text)) {
                    return em;
                }
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }
}
