package com.github.cuteluobo.livedanmuarchive.enums.danmu.send;

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


    private final String name;
    private final String commit;

    public String getCommit() {
        return commit;
    }

    public String getName() {
        return name;
    }
}
