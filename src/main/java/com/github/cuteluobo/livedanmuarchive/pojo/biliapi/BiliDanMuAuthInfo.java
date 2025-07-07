package com.github.cuteluobo.livedanmuarchive.pojo.biliapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * B站弹幕认证包信息
 *
 * @author: CuteLuoBo
 * @date: 2025/7/6  20:27
 * @version: 1.0.0
 */
public class BiliDanMuAuthInfo {
    private String token;
    private JsonNode hostList;

    public BiliDanMuAuthInfo() {
    }

    public BiliDanMuAuthInfo(String token, JsonNode hostList) {
        this.token = token;
        this.hostList = hostList;
    }

    @Override
    public String toString() {
        return "BiliDanMuAuthInfo{" +
                "token='" + token + '\'' +
                ", hostList=" + hostList +
                '}';
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public JsonNode getHostList() {
        return hostList;
    }

    public void setHostList(JsonNode hostList) {
        this.hostList = hostList;
    }
}
