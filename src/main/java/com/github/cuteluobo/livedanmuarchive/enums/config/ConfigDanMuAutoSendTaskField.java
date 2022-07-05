package com.github.cuteluobo.livedanmuarchive.enums.config;

import com.github.cuteluobo.livedanmuarchive.enums.danmu.send.StartMode;
import com.github.cuteluobo.livedanmuarchive.enums.danmu.send.VideoPlatform;
import com.qq.tars.common.util.Config;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 弹幕自动发送基础配置字段
 * @author CuteLuoBo
 * @date 2022/7/4 11:33
 */
public enum ConfigDanMuAutoSendTaskField implements ConfigField {
    /***/
    MAIN_FIELD("DanMuAutoSendTask","弹幕自动发送任务设置",true),
    DEPLOY_LIST("deployList","部署列表",true),
    VIDEO_PLATFORM("videoPlatform"
            ,"视频平台("+ Arrays.stream(VideoPlatform.values()).map(u -> u.getName()+"-"+u.getCommit()).collect(Collectors.joining(","))+")"
            ,VideoPlatform.BILIBILI.getName()
    ),
    LISTEN_UP_UID("listenUpUid","监听的上传者UID","0"),
    TITLE_MATCH("titleMatch","标题匹配字符","【录播】"),
    TAG_MATCH("tagMatch","标签匹配字符","autoDanMu"),
    VIDEO_P_TIME_FORMAT("videoPTimeFormat","视频分P时间储存格式(正则)","\\s*([0-9]{4,}-[0-1]*[0-9]-[0-3]*[0-9]T[0-2][0-9]_[0-6][0-9]_[0-6][0-9])"),
    VIDEO_P_NORMAL_MINUTE("videoPNormalMinute","视频分P单集默认时间（分钟）","60"),
    VIDEO_P_ALLOW_TIME_FLUCTUATE("videoPAllowTimeFluctuate","视频分P单集允许的波动时间(秒）","120"),
    LINK_DANMU_SAVE_NAME("linkDanMuSaveName","链接的弹幕保存名称","xxxx"),
    START_MODE("startMode"
            ,"启动模式 ("+ Arrays.stream(StartMode.values()).map(u -> u.getValue()+"-"+u.getComment()).collect(Collectors.joining(","))+")"
            ,String.valueOf(StartMode.MANUAL.getValue())
    )
            ;

    ConfigDanMuAutoSendTaskField(String fieldString, String comment, String normalValue, boolean mainField) {
        this.fieldString = fieldString;
        this.comment = comment;
        this.normalValue = normalValue;
        this.mainField = mainField;
    }

    ConfigDanMuAutoSendTaskField(String fieldString, String comment, String normalValue) {
        this.fieldString = fieldString;
        this.comment = comment;
        this.normalValue = normalValue;
        this.mainField = false;
    }
    ConfigDanMuAutoSendTaskField(String fieldString, String comment) {
        this.fieldString = fieldString;
        this.comment = comment;
        this.normalValue = "";
        this.mainField = false;
    }
    ConfigDanMuAutoSendTaskField(String fieldString, String comment, boolean mainField) {
        this.fieldString = fieldString;
        this.comment = comment;
        this.normalValue = "";
        this.mainField = mainField;
    }

    ConfigDanMuAutoSendTaskField(String fieldString) {
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
