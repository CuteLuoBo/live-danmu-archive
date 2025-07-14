package com.github.cuteluobo.livedanmuarchive.utils;

import cn.hutool.core.util.StrUtil;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * B站登录与登录状态认证
 * @author CuteLuoBo
 * @date 2022/12/26 15:51
 */
public class BiliLoginUtil {
    static Logger logger = LoggerFactory.getLogger(BiliLoginUtil.class);
    public static final String LOGIN_STATUS_API = "http://api.bilibili.com/x/web-interface/nav/stat";
    public static final String CK_USER_BASE_INFO_API = "https://api.bilibili.com/x/web-interface/nav";
    public static final String ACKEY_USER_BASE_INFO_API = "https://app.bilibili.com/x/v2/account/myinfo";

    /**
     * APPkey与盐值
     * https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/misc/sign/APPKey.md
     */
    public static final Map<String, String> APP_KEY_MAP = Map.of(
            "4409e2ce8ffd12b8","59b43e04ad6965f34319062b478f83dd",
            "1d8b6e7d45233436","560c52ccd288fed045859ed18bffd973",
            "783bbb7264451d82","2653583c8873dea268ab9386918b1d65"
    );
    /**
     * 登录查询
     * @param sessData CK方式登录
     * @return 查询结果
     * @throws URISyntaxException URI创建错误
     */
    public static boolean checkLogin(String sessData){
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
     * https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/login/login_info.md
     */
    public static BaseUserInfo getUserBaseInfoByCk(String ck) {
        BaseUserInfo baseUserInfo = new BaseUserInfo();
        reactor.netty.http.client.HttpClient httpClient = reactor.netty.http.client.HttpClient.create();
        try {
            String body = httpClient.headers(headers -> {
                                headers.add("Cookie", ck);
                                headers.add("Content-Type", "application/x-www-form-urlencoded");
                            }
                    ).get()
                    .uri(new URI(CK_USER_BASE_INFO_API))
                    .responseContent().aggregate().asString().block();
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
            baseUserInfo.setUid(midNode.asLong());
            //等级
            JsonNode levelNode = dataNode.get("level_info");
            JsonNode currentLevelNode = levelNode.get("current_level");
            int level = currentLevelNode.asInt();
            baseUserInfo.setLevel(level);
            //昵称
            JsonNode uNameNode = dataNode.get("uname");
            String nickName = uNameNode.asText();
            baseUserInfo.setNickName(nickName);
            //验证Key https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/login/login_info.md#%E5%AF%BC%E8%88%AA%E6%A0%8F%E7%94%A8%E6%88%B7%E4%BF%A1%E6%81%AF
            JsonNode wbiNode = dataNode.get("wbi_img");
            String imgKey = StrUtil.subBetween(wbiNode.get("img_url").asText(),"wbi/",".png");
            String subKey = StrUtil.subBetween(wbiNode.get("sub_url").asText(),"wbi/",".png");
            baseUserInfo.setImgKey(imgKey);
            baseUserInfo.setSubKey(subKey);
            baseUserInfo.setCookie(ck);
        } catch (Exception e) {
            logger.error("请求接口获取获取用户信息时发生错误:",e);
        }
        return baseUserInfo;
    }

    /**
     * 获取账号数据（app渠道）
     * @param accessKey  APP登录Token
     * @param appKey     APP密钥
     * @param appSec     APP盐值
     * @return 查询结果
     * @throws URISyntaxException
     * https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/login/login_info.md
     * https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/other/API_sign.md
     */
    public static BaseUserInfo getUserBaseInfoByAppKey(String accessKey, String appKey,String appSec) throws URISyntaxException {
        if (StrUtil.isEmpty(accessKey) || StrUtil.isEmpty(appKey) || StrUtil.isEmpty(appSec)) {
            return new BaseUserInfo();
        }
        if (appSec == null || appSec.trim().isEmpty()) {
            appSec = APP_KEY_MAP.get(appKey);
            if (appSec == null) {
                throw new NoSuchFieldError("没有配置appsec，同时在KEY中没有找到appsec，无法使用APP渠道登录");
            }
        }
        Map<String, String> params = new TreeMap<>();
        params.put("access_key", accessKey);
        params.put("appkey", appKey);
        long time = System.currentTimeMillis();
        params.put("ts", String.valueOf(time));
        String sign = appSign(params, appKey, appSec);
        BaseUserInfo baseUserInfo = new BaseUserInfo();
        baseUserInfo.setAccessKey(accessKey);
        try {
            String url = ACKEY_USER_BASE_INFO_API
                    + "?"
                    + String.format("access_key=%s&appkey=%s&ts=%s&sign=%s", accessKey, appKey, time, sign);

            String body = NettyLinkUtil.getHtmlBodyByNetty(url,null);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode bodyNode = objectMapper.readTree(body);
            JsonNode codeNode = bodyNode.get("code");
            int code = codeNode.asInt(-1);
            logger.debug("B站-acKey用户信息获取API返回信息：code:{},message:{}",code,bodyNode.get("message").asText());
            if (code == 0) {
                baseUserInfo.setLogin(true);
            } else {
                logger.error("B站-acKey用户信息获取失败，返回信息：code:{},message:{}",code,bodyNode.get("message").asText());
                return baseUserInfo;
            }
            JsonNode dataNode = bodyNode.get("data");
            //uid
            JsonNode midNode = dataNode.get("mid");
            baseUserInfo.setUid(midNode.asInt());
            //等级
            JsonNode levelNode = dataNode.get("level");
            int level = levelNode.asInt();
            baseUserInfo.setLevel(level);
            //昵称
            JsonNode nameNode = dataNode.get("name");
            String nickName = nameNode.asText();
            baseUserInfo.setNickName(nickName);
        } catch (Exception e) {
            logger.error("请求接口获取获取用户等级时发生错误:",e);
        }
        return baseUserInfo;
    }

    public static String appSign(Map<String, String> params, @NotNull String appKey,@NotNull String appSec) {
        // 为请求参数进行 APP 签名
        params.put("appkey", appKey);
        // 按照 key 重排参数
        Map<String, String> sortedParams = new TreeMap<>(params);
        // 序列化参数
        StringBuilder queryBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append('&');
            }
            queryBuilder
                    .append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return generateMD5(queryBuilder .append(appSec).toString());
    }

    private static String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 登录查询
     * @param sessData CK方式登录
     * @param accessKey App方式登录
     * @return 查询结果
     */
    private static boolean checkLogin(String sessData, String accessKey){
        Map<String,Object> headers = new HashMap<>();
        String url = LOGIN_STATUS_API;
        if (sessData != null) {
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            headers.put("Cookie", sessData);
        } else if (accessKey != null) {
            url = LOGIN_STATUS_API + URLEncoder.encode("?access_key=" + accessKey, StandardCharsets.UTF_8);
        } else {
            return false;
        }
        try {
            String body = NettyLinkUtil.getHtmlBodyByNetty(url, headers);
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
