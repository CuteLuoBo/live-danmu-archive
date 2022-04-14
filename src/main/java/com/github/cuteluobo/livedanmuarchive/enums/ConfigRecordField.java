package com.github.cuteluobo.livedanmuarchive.enums;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 配置文件录制字段
 *
 * @author CuteLuoBo
 * @date 2022/4/14 15:13
 */
public enum ConfigRecordField {
    /***/
    MAIN_FIELD("Record","录制记录"),
    RECORD_LIST("recordList","录制列表"),
    RECORD_LIST_UNIT("recordListUnit","单个录制分块"),
    ROOM_URL("roomUrl","直播间录制url"),
    RECORD_TYPE("recordType","录制类型（弹幕/礼物/视频等，待拓展此类暂时无效）"),
    SAVE_NAME("saveName","保存文件名称"),
    DANMU_EXPORT_TYPE("danMuExportType","导出类型模式"+" ("+ Arrays.stream(DanMuExportType.values()).map(DanMuExportType::getValue).collect(Collectors.joining(","))+")"),
    EXPORT_PATTERN("exportPatten","导出文件存档模式"+" ("+ Arrays.stream(ExportPattern.values()).map(ExportPattern::getText).collect(Collectors.joining(","))+")"),
    DANMU_RECORD_RETRY_TIME("danmuRecordRetryTime","重试时间(单位：秒，设置为-1时不进行重试)"),
    ;

    ConfigRecordField(String fieldString, String comment) {
        this.fieldString = fieldString;
        this.comment = comment;
    }
    ConfigRecordField(String fieldString) {
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
