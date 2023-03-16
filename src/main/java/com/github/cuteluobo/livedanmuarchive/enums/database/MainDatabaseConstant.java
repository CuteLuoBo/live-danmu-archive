package com.github.cuteluobo.livedanmuarchive.enums.database;

/**
 * 主数据库常量
 * @author CuteLuoBo
 * @date 2023/3/15 11:08
 */
public enum MainDatabaseConstant {
    /***/
    TABLE_DANMU_ACCOUNT_TASK("danmu_account_task"),TABLE_DANMU_SENDER_TASK("danmu_sender_task")
    ;
    private String value;

    MainDatabaseConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
