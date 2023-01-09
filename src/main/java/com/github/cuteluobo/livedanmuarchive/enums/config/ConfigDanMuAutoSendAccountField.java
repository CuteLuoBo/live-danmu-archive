package com.github.cuteluobo.livedanmuarchive.enums.config;

import com.github.cuteluobo.livedanmuarchive.enums.danmu.send.StartMode;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 弹幕自动发送的小号配置
 * TODO 完成配置
 * @author CuteLuoBo
 * @date 2022/7/4 18:29
 */
public enum ConfigDanMuAutoSendAccountField {
    /***/
    MAIN_FIELD("DanMuAutoSendAccount","弹幕自动发送设置"),
    DEPLOY_LIST("accountList","账号列表"),
    DEPLOY_LIST_UNIT("accountListUnit","单个账号分块"),
    VIDEO_PLATFORM("videoPlatform"
            ,"视频平台 ("
            + Arrays.stream(StartMode.values()).map(u -> u.getValue()+"-"+u.getComment()).collect(Collectors.joining(","))
            +")"),
    USER_NAME("userName","账户名"),
    PASSWORD("password","登录密码"),
    COOKIES("cookies","登录cookies"),
    ACCESS_KEY("accessKey","客户端密钥"),
    APP_KEY("appKey","accessKey对应的应用key"),
    ;

    ConfigDanMuAutoSendAccountField(String fieldString, String comment) {
        this.fieldString = fieldString;
        this.comment = comment;
    }
    ConfigDanMuAutoSendAccountField(String fieldString) {
        this.fieldString = fieldString;
        this.comment = "";
    }

    public String getFieldString() {
        return fieldString;
    }

    public String getComment() {
        return comment;
    }

    private final String fieldString;
    private final String comment;
}
