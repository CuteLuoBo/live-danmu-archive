package com.github.cuteluobo.livedanmuarchive.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * B站登录与登录状态认证
 * @author CuteLuoBo
 * @date 2022/12/26 15:51
 */
public class BiliLoginUtil {
    static Logger logger = LoggerFactory.getLogger(BiliLoginUtil.class);
    public static final String LOGIN_STATUS_API = "http://api.bilibili.com/x/web-interface/nav/stat";

    /**
     * 登录查询
     * @param sessData CK方式登录
     * @return 查询结果
     * @throws URISyntaxException URI创建错误
     */
    public static boolean checkLogin(String sessData) throws URISyntaxException {
        return checkLogin(sessData, null);
    }

    /**
     * 登录查询
     * @param accessKey App方式登录
     * @return 查询结果
     * @throws URISyntaxException URI创建错误
     */
    public static boolean checkLoginByAk(String accessKey) throws URISyntaxException {
        return checkLogin(null, accessKey);
    }

    /**
     * 登录查询
     * @param sessData CK方式登录
     * @param accessKey App方式登录
     * @return 查询结果
     * @throws URISyntaxException URI创建错误
     */
    private static boolean checkLogin(String sessData, String accessKey) throws URISyntaxException {
        HttpClient httpClient = LinkUtil.getNormalHttpClient();
        HttpRequest httpRequest;
        if (sessData != null) {
            httpRequest = HttpRequest.newBuilder(new URI(LOGIN_STATUS_API))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Cookie", sessData)
                    .GET()
                    .build();
        } else if (accessKey != null) {
            httpRequest = HttpRequest.newBuilder(new URI(LOGIN_STATUS_API+ URLEncoder.encode("?access_key="+accessKey, StandardCharsets.UTF_8)))
                    .GET()
                    .build();
        } else {
            return false;
        }
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String body = httpResponse.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode bodyNode = objectMapper.readTree(body);
            JsonNode node = bodyNode.get("code");
            int code = node.asInt(-1);
            return code == 0;
        } catch (Exception e) {
            logger.error("请求接口验证用户在线状态时发生错误:",e);
            return false;
        }
    }
}
