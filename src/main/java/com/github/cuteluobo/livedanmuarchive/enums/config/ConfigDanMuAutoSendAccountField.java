package com.github.cuteluobo.livedanmuarchive.enums.config;

import com.github.cuteluobo.livedanmuarchive.enums.danmu.send.StartMode;
import com.github.cuteluobo.livedanmuarchive.enums.danmu.send.VideoPlatform;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 弹幕自动发送的小号配置
 * @author CuteLuoBo
 * @date 2022/7/4 18:29
 */
public enum ConfigDanMuAutoSendAccountField implements ConfigField  {
    /***/
    MAIN_FIELD("DanMuAutoSendAccount","弹幕自动发送账号设置",true),
    DEPLOY_LIST("accountList","账号列表",true),
    VIDEO_PLATFORM("videoPlatform"
            ,"视频平台 ("
            + Arrays.stream(VideoPlatform.values()).map(u -> u.getName()+"-"+u.getCommit()).collect(Collectors.joining(","))
            +")", VideoPlatform.BILIBILI.getName()),
    NICK_NAME("nickName","昵称(用于未登录时显示，登陆后会自动更新)",""),
    USER_NAME("userName","账户名(暂无效)",""),
    PASSWORD("password","登录密码(暂无效)",""),
    COOKIES("cookies","登录cookies","SESSDATA=xxx; bili_jct=xxx; DedeUserID=xxx; DedeUserID__ckMd5=xxx;"),
    ACCESS_KEY("accessKey","客户端密钥"),
    APP_KEY("appKey","accessKey对应的应用key"),
    ;

    ConfigDanMuAutoSendAccountField(String fieldString, String comment, String normalValue, boolean mainField) {
        this.fieldString = fieldString;
        this.comment = comment;
        this.normalValue = normalValue;
        this.mainField = mainField;
    }

    ConfigDanMuAutoSendAccountField(String fieldString, String comment, String normalValue) {
        this.fieldString = fieldString;
        this.comment = comment;
        this.normalValue = normalValue;
        this.mainField = false;
    }
    ConfigDanMuAutoSendAccountField(String fieldString, String comment) {
        this.fieldString = fieldString;
        this.comment = comment;
        this.normalValue = "";
        this.mainField = false;
    }
    ConfigDanMuAutoSendAccountField(String fieldString, String comment, boolean mainField) {
        this.fieldString = fieldString;
        this.comment = comment;
        this.normalValue = "";
        this.mainField = mainField;
    }

    ConfigDanMuAutoSendAccountField(String fieldString) {
        this.fieldString = fieldString;
        this.comment = "";
        this.normalValue = "";
        this.mainField = false;
    }

    @Override
    public String getFieldString() {
        return fieldString;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String getNormalValue() {
        return normalValue;
    }

    @Override
    public boolean isMainField() {
        return mainField;
    }

    /**
     * 获取列表头
     *
     * @return 列表头
     */
    @Override
    public ConfigField getListHeader() {
        return DEPLOY_LIST;
    }

    /**
     * 获取主开头
     *
     * @return 主开头
     */
    @Override
    public ConfigField getMainField() {
        return MAIN_FIELD;
    }

    private final String fieldString;
    private final String comment;
    private final String normalValue;
    private final boolean mainField;
}
