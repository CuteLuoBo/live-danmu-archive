package com.github.cuteluobo.livedanmuarchive.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseUserInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * B站登录与登录状态认证
 * @author CuteLuoBo
 * @date 2022/12/26 15:51
 */
public class BiliLoginUtil {
    static Logger logger = LoggerFactory.getLogger(BiliLoginUtil.class);
    public static final String LOGIN_STATUS_API = "http://api.bilibili.com/x/web-interface/nav/stat";
    public static final String CK_USER_BASE_INFO_API = "https://api.bilibili.com/nav";
    public static final String ACKEY_USER_BASE_INFO_API = "https://app.bilibili.com/x/v2/account/myinfo";
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
     * 获取账号数据(CK)
     * @param ck cookies
     * @return 查询结果
     * @throws URISyntaxException
     * https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/login/login_info.md
     */
    public static BaseUserInfo getUserBaseInfoByCk(@NotNull String ck) throws URISyntaxException {
        BaseUserInfo baseUserInfo = new BaseUserInfo();
        HttpClient httpClient = LinkUtil.getNormalHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder(new URI(CK_USER_BASE_INFO_API))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", ck)
                .GET()
                .build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String body = httpResponse.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode bodyNode = objectMapper.readTree(body);
            JsonNode codeNode = bodyNode.get("code");
            int code = codeNode.asInt(-1);
            logger.debug("B站-CK用户信息获取API返回信息：code:{},message:{}",code,bodyNode.get("message").asText());
            if (code == 0) {
                baseUserInfo.setLogin(true);
            } else {
                return baseUserInfo;
            }
            JsonNode dataNode = bodyNode.get("data");
            //uid
            JsonNode midNode = dataNode.get("mid");
            baseUserInfo.setUid(midNode.asInt());
            //等级
            JsonNode levelNode = dataNode.get("level_info");
            JsonNode currentLevelNode = levelNode.get("current_level");
            int level = currentLevelNode.asInt();
            baseUserInfo.setLevel(level);
            //昵称
            JsonNode uNameNode = dataNode.get("uname");
            String nickName = uNameNode.asText();
            baseUserInfo.setNickName(nickName);
        } catch (Exception e) {
            logger.error("请求接口获取获取用户等级时发生错误:",e);
        }
        return baseUserInfo;
    }

    /**
     * 获取账号数据（app渠道）
     * @param accessKey  APP登录Token
     * @param appKey     APP密钥
     * @param appsec     APP盐值
     * @return 查询结果
     * @throws URISyntaxException
     * https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/login/login_info.md
     * https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/other/API_sign.md
     */
    public static BaseUserInfo getUserBaseInfoByAppKey(@NotNull String accessKey, @NotNull String appKey, @NotNull String appsec) throws URISyntaxException {
        throw new RuntimeException("此方法暂未实现");
        //TODO 待实现
//        BaseUserInfo baseUserInfo = new BaseUserInfo();
//        HttpClient httpClient = LinkUtil.getNormalHttpClient();
//        HttpRequest httpRequest = HttpRequest.newBuilder(new URI(ACKEY_USER_BASE_INFO_API
//                        +"?"
//                        +String.format("access_key=%s&appkey=%s&sign=%s&ts=%s",accessKey,appKey,?,System.currentTimeMillis())))
//                .GET()
//                .build();
//        try {
//            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
//            String body = httpResponse.body();
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode bodyNode = objectMapper.readTree(body);
//            JsonNode codeNode = bodyNode.get("code");
//            int code = codeNode.asInt(-1);
//            logger.debug("B站-acKey用户信息获取API返回信息：code:{},message:{}",code,bodyNode.get("message").asText());
//            if (code == 0) {
//                baseUserInfo.setLogin(true);
//            } else {
//                return baseUserInfo;
//            }
//            JsonNode dataNode = bodyNode.get("data");
//            //uid
//            JsonNode midNode = dataNode.get("mid");
//            baseUserInfo.setUid(midNode.asInt());
//            //等级
//            JsonNode levelNode = dataNode.get("level");
//            int level = levelNode.asInt();
//            baseUserInfo.setLevel(level);
//            //昵称
//            JsonNode nameNode = dataNode.get("name");
//            String nickName = nameNode.asText();
//            baseUserInfo.setNickName(nickName);
//        } catch (Exception e) {
//            logger.error("请求接口获取获取用户等级时发生错误:",e);
//        }
//        return baseUserInfo;
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
