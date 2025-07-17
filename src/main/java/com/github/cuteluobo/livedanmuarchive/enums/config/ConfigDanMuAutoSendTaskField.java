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
    VIDEO_P_TIME_FORMAT("videoPTimeFormat","视频分P时间解析格式","yyyy-MM-dd' 'HH_mm_ss",true),
    VIDEO_P_TIME_REGULAR("videoPTimeRegular","视频分P时间正则匹配格式()","\\s*([0-9]{4,}-[0-1]*[0-9]-[0-3]*[0-9][T\\s][0-2][0-9]_[0-5][0-9]_[0-5][0-9])",true),
    DEPLOY_LIST("deployList","部署列表",true),
    VIDEO_PLATFORM("videoPlatform"
            ,"视频平台("+ Arrays.stream(VideoPlatform.values()).map(u -> u.getName()+"-"+u.getCommit()).collect(Collectors.joining(","))+")"
            ,VideoPlatform.BILIBILI.getName()
    ),
    LISTEN_UP_UID("listenUpUid","监听的上传者UID(为0时关闭监听)","0"),
    LISTEN_DELAY_TIME("listenDelayTime","监听的延迟时间(秒)，默认300s=5分","300"),
    TITLE_MATCH("titleMatch","标题匹配字符","【录播】"),
    TAG_MATCH("tagMatch","标签匹配字符，以英文逗号(,)分割","autoDanMu"),
    LINK_DANMU_SAVE_NAME("linkDanMuSaveName","链接的弹幕保存名称","xxxx"),
    DANMU_START_ROUND("danMuStartRound","弹幕开始轮数(默认0，设定轮数范围以适配分机运行) ","0",true),
    DANMU_END_ROUND("danMuEndRound","弹幕结束轮数默认2，范围为[0,2)","2",true),
    DANMU_SPLIT_TIME("danMuSplitTime","弹幕分片时间(ms)，时间越短弹幕越密集，根据自身弹幕账号资源而定，默认5000=5s","5000",true),
    DANMU_ALLOW_PEAK_TIME("danMuAllowPeakTime","是否允许高能时间（触发后当前高能时间相关弹幕都将放在第0轮中发送）","true",true),
    DANMU_PEAK_TIME_THRESHOLD("danMuPeakTimeThreshold","分片时间内的高能时间触发阈值","20",true),
    DANMU_PEAK_TIME_MAX("danMuPeakTimeMax","分片时间内的高能时间弹幕最大数量(过多的将丢弃)","15",true),
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
