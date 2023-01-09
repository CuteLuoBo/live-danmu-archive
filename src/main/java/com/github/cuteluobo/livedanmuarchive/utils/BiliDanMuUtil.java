package com.github.cuteluobo.livedanmuarchive.utils;

import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * B站弹幕工具类
 *  TODO 将BAS输出转为工具类，然后通过一个B站弹幕发送服务类调用发送并解析结果
 * @author CuteLuoBo
 * @date 2022/11/28 17:05
 */
public class BiliDanMuUtil {
    public static final String DANMU_API = "http://api.bilibili.com/x/v2/dm/post";
    public static final Pattern CSRF_PATTERN = Pattern.compile("bili_jct=([^;]*);");

    //TODO 完成指定上传者的动态监测，识别后增加任务进行弹幕发送，同时规划一个区分进度的断点检测
    /**
     * 发送弹幕信息
     * https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/danmaku/action.md#%E5%8F%91%E9%80%81%E8%A7%86%E9%A2%91%E5%BC%B9%E5%B9%95
     * @param oid       视频cid
     * @param msg        内容
     * @param bvId       BV ID，两者择一
     * @param avId       AV ID，两者择一
     * @param progress   视频时间（ms），可选
     * @param color      十进制颜色，可选
     * @param fontSize   字体大小，可选，默认25
     * @param pool       弹幕池，可选
     * @param mode       弹幕模式，可选
     * @param cookie     网页CK，两者择一
     * @param accessKey APP Token，两者择一
     * @return 请求结果
     * @throws URISyntaxException  创建访问链接错误
     * @throws IOException      网络IO错误
     * @throws InterruptedException 线程中断错误
     */
    public static HttpResponse<String> sendDanMu(long oid, @NotNull String msg, String bvId, int avId, Long progress, Integer color, Float fontSize, Integer pool, Integer mode, String cookie, String accessKey) throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = LinkUtil.getNormalHttpClient();
        Map<String, Object> postDataMap = new LinkedHashMap<>();
        //视频弹幕
        postDataMap.put("type", 1);
        postDataMap.put("oid", oid);
        postDataMap.put("msg", msg);
        postDataMap.put("rnd", System.currentTimeMillis()*1000000);
        if (bvId != null) {
            postDataMap.put("bvid", bvId);
        } else {
            if (avId <= 0) {
                throw new IllegalArgumentException("bvId和avId两者必须具有一个有效值");
            }
            postDataMap.put("aid", avId);
        }
        if (progress != null) {
            postDataMap.put("progress", progress);
        }
        if (color != null) {
            postDataMap.put("color", color);
        }
        if (fontSize != null) {
            postDataMap.put("fontSize", fontSize);
        }
        if (pool != null) {
            postDataMap.put("pool", pool);
        }
        if (mode != null) {
            postDataMap.put("mode", mode);
        }
        HttpRequest httpRequest;
        if (cookie != null) {
            Matcher mather = CSRF_PATTERN.matcher(cookie);
            if (mather.find()) {
                String csrf = mather.group(1);
                postDataMap.put("csrf", csrf);
                httpRequest = HttpRequest.newBuilder(new URI(DANMU_API))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("cookie",cookie)
                        .POST(HttpRequest.BodyPublishers.ofString(FormatUtil.encodePostStringByMap(postDataMap)))
                        .build();
            } else {
                throw new IllegalArgumentException("传入的CK格式不正确，无法找到bili_jct(csrf)");
            }
        } else {
            postDataMap.put("access_key", accessKey	);
            httpRequest = HttpRequest.newBuilder(new URI(DANMU_API))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(FormatUtil.encodePostStringByMap(postDataMap)))
                    .build();
        }
        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
