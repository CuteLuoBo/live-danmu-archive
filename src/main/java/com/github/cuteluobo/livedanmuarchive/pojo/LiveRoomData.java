package com.github.cuteluobo.livedanmuarchive.pojo;

import com.github.cuteluobo.livedanmuarchive.enums.WebsiteType;

/**
 * 直播间数据
 *
 * @author CuteLuoBo
 * @date 2022/4/13 16:26
 */
public class LiveRoomData {
    /**
     * 直播间平台类型
     */
    private WebsiteType websiteType;
    /**
     * 传入直播间完整URL
     */
    private String liveRoomUrl;
    /**
     * 直播间代号
     */
    private String liveRoomCode;
    /**
     * 直播间主播名称
     */
    private String liveAnchorName;

    public WebsiteType getWebsiteType() {
        return websiteType;
    }

    public void setWebsiteType(WebsiteType websiteType) {
        this.websiteType = websiteType;
    }

    public String getLiveRoomUrl() {
        return liveRoomUrl;
    }

    public void setLiveRoomUrl(String liveRoomUrl) {
        this.liveRoomUrl = liveRoomUrl;
    }

    public String getLiveRoomCode() {
        return liveRoomCode;
    }

    public void setLiveRoomCode(String liveRoomCode) {
        this.liveRoomCode = liveRoomCode;
    }

    public String getLiveAnchorName() {
        return liveAnchorName;
    }

    public void setLiveAnchorName(String liveAnchorName) {
        this.liveAnchorName = liveAnchorName;
    }
}
