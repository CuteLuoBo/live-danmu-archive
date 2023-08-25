package com.github.cuteluobo.livedanmuarchive.pojo;


import com.github.cuteluobo.livedanmuarchive.enums.DanMuMessageType;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuFormatModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuUserInfoModel;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 弹幕数据
 *
 * @author CuteLuoBo
 * @date 2021/12/16 16:22
 */
public class DanMuData {
    /**
     * 发送者名称
     */
    private DanMuUserInfo userIfo;
    /**弹幕信息*/
    private String content;
    /**弹幕格式*/
    private DanMuFormat danMuFormatData;
    /**发送时间(时间戳类型)*/
    private Long timestamp;
    /**消息类型(弹幕/礼物/其他)*/
    private String msgType;

    public DanMuData() {
    }

    public DanMuData(String content) {
        this.content = content;
    }

    /**
     * 返回默认消息
     * @return 返回默认消息
     */
    public String toNormalString() {
        return (userIfo == null ? "?" : userIfo) + content;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DanMuData{");
        if (userIfo != null) {
            sb.append("userIfo-nickName=").append(userIfo.getNickName());
        }
        sb.append(", content='").append(content).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", msgType='").append(msgType).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * 转换数据
     * @param danMuDataModel 弹幕数据库数据
     * @return 转换结果
     */
    public static DanMuData convent(@NotNull DanMuDataModel danMuDataModel) {
        DanMuData danMuData = new DanMuData();
        danMuData.content = danMuDataModel.getData();
        danMuData.timestamp = danMuDataModel.getCreateTime();
        danMuData.setMsgType(Optional.ofNullable(DanMuMessageType.getEnumByTypeValue(danMuDataModel.getType())).orElse(DanMuMessageType.OTHER).getText());
        return danMuData;
    }

    /**
     * 转换数据
     * @param danMuDataModel 弹幕数据库数据
     * @param danMuUserInfoModel 弹幕用户数据
     * @param danMuFormatModel   弹幕样式数据
     * @return 转换结果
     */
    public static DanMuData convent(@NotNull DanMuDataModel danMuDataModel, DanMuUserInfoModel danMuUserInfoModel, DanMuFormatModel danMuFormatModel) {
        DanMuData danMuData = convent(danMuDataModel);
        danMuData.setDanMuFormatData(danMuFormatModel);
        danMuData.setUserIfo(danMuUserInfoModel);
        return danMuData;
    }

    public DanMuUserInfo getUserIfo() {
        return userIfo;
    }

    public void setUserIfo(DanMuUserInfo userIfo) {
        this.userIfo = userIfo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public DanMuFormat getDanMuFormatData() {
        return danMuFormatData;
    }

    public void setDanMuFormatData(DanMuFormat danMuFormatData) {
        this.danMuFormatData = danMuFormatData;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
}
