package com.github.cuteluobo.livedanmuarchive.enums;

/**
 * 弹幕数据库常量
 *
 * @author CuteLuoBo
 * @date 2022/4/5 15:50
 */
public enum DanMuDatabaseConstant {
    /***/
    TABLE_USER_INFO("user_info"),TABLE_DANMU_DATA("danmu_data"),TABLE_DANMU_FORMAT("danmu_format")
    ;
    private String value;

    DanMuDatabaseConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
