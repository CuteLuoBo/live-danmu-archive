package com.github.cuteluobo.livedanmuarchive.pojo;


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
