package com.github.cuteluobo.livedanmuarchive.enums;

import lombok.Getter;

/**
 * 网站类型
 *
 * @author CuteLuoBo
 * @date 2021/12/16 16:43
 */
@Getter
public enum WebsiteType {
    /**枚举类型*/
    Huya("虎牙", "huya")
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
}
