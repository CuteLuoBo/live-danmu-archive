package com.github.cuteluobo.livedanmuarchive.enums;

import java.util.Objects;

/**
 * @author CuteLuoBo
 * @date 2022/4/12 11:58
 */
public enum DanMuExportType {
    /***/
    SQLITE("sqlite"),JSON("json")
//    ,MYSQL("mysql")
    ;
    private final String value;

    DanMuExportType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DanMuExportType getEnumByValue(String text) {
        if (text != null) {
            for (DanMuExportType em : DanMuExportType.values()) {
                if (Objects.equals(em.getValue(), text)) {
                    return em;
                }
            }
        }
        return null;
    }
}
