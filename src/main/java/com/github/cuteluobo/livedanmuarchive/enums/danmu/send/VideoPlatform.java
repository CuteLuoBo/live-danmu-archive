package com.github.cuteluobo.livedanmuarchive.enums.danmu.send;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuExportType;

import java.util.Objects;

/**
 * 支持的发送平台
 *
 * @author CuteLuoBo
 * @date 2022/7/4 20:46
 */
public enum VideoPlatform {
    /***/
    BILIBILI("bili","B站"),
    ;

    VideoPlatform(String name, String commit) {
        this.name = name;
        this.commit = commit;
    }

    public static VideoPlatform getEnumByName(String text) {
        if (text != null) {
            for (VideoPlatform em : VideoPlatform.values()) {
                if (Objects.equals(em.getName(), text)) {
                    return em;
                }
            }
        }
        return null;
    }


    private final String name;
    private final String commit;

    public String getCommit() {
        return commit;
    }

    public String getName() {
        return name;
    }


}
