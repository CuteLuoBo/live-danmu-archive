package com.github.cuteluobo.livedanmuarchive.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * B站弹幕工具类
 *  TODO 将BAS输出转为工具类，然后通过一个B站弹幕发送服务类调用发送并解析结果
 * @author CuteLuoBo
 * @date 2022/11/28 17:05
 */
public class BiliDanMuUtil {
    private static final Logger logger = LoggerFactory.getLogger(BiliDanMuUtil.class);
    public static final String DANMU_API = "http://api.bilibili.com/x/v2/dm/post";
    public static final Pattern CSRF_PATTERN = Pattern.compile("bili_jct=([^;]*);");
    private static TimeInterval timer = DateUtil.timer();
    private static  String timerMark1 = "参数初始化";
    private static String timerMark2 = "请求发送与解析";
    /**
     * 发送弹幕信息
     * https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/danmaku/action.md
     * @param cid       视频cid
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
    public static String sendDanMuByNetty(long cid, @NotNull String msg, String bvId, int avId, Long progress, Integer color, Float fontSize, Integer pool, Integer mode,String imgKey,String subKey, String cookie, String accessKey) throws URISyntaxException, IOException, InterruptedException {
        timeMark1();
        reactor.netty.http.client.HttpClient httpClient = NettyLinkUtil.getNormalHttpClient();
        Map<String, Object> postDataMap = new LinkedHashMap<>();
        //视频弹幕
        postDataMap.put("type", 1);
        postDataMap.put("oid", cid);
        postDataMap.put("msg", msg);
        postDataMap.put("rnd", System.currentTimeMillis()+"000000");
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
                //构建url与签名
                TreeMap<String, Object> urlParamMap = new TreeMap<>();
                long wts = System.currentTimeMillis() / 1000;
                urlParamMap.put("csrf", csrf);
                urlParamMap.put("wts", wts);
                String param = urlParamMap.entrySet().stream()
                        .map(it -> String.format("%s=%s", it.getKey(), BiliInfoUtil.encodeURIComponent(it.getValue())))
                        .collect(Collectors.joining("&"));
                String wRid = BiliInfoUtil.createWRid(wts, param, imgKey, subKey);
                String url = DANMU_API +"?"+ param + "&w_rid=" + wRid;
                //性能分析埋桩
                timeMark2();
                String response = httpClient
                        .headers(it -> {
                            it.set(HttpHeaderNames.COOKIE, cookie);
                            it.set(HttpHeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded");
                        })
                        .post()
                        .uri(new URI(url))
                        .send(ByteBufFlux.fromString(Mono.just(FormatUtil.encodePostStringByMap(postDataMap))))
                        .responseContent().aggregate().asString().block();
                timeMarkPrintLog();
                return response;
            } else {
                throw new IllegalArgumentException("传入的CK格式不正确，无法找到bili_jct(csrf)");
            }
        } else {
            TreeMap<String, Object> urlParamMap = new TreeMap<>();
            long wts = System.currentTimeMillis() / 1000;
            urlParamMap.put("wts", wts);
            String param = urlParamMap.entrySet().stream()
                    .map(it -> String.format("%s=%s", it.getKey(), BiliInfoUtil.encodeURIComponent(it.getValue())))
                    .collect(Collectors.joining("&"));
            String wRid = BiliInfoUtil.createWRid(wts, param, imgKey, subKey);
            String url = DANMU_API +"?"+ param + "&w_rid=" + wRid;
            //性能分析埋桩
            timeMark2();
            String response = httpClient
                    .headers(it -> {
                        it.set("access_key", accessKey);
                        it.set(HttpHeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded");
                    })
                    .post()
                    .uri(new URI(url))
                    .send(ByteBufFlux.fromString(Mono.just(FormatUtil.encodePostStringByMap(postDataMap))))
                    .responseContent().aggregate().asString().block();
            timeMarkPrintLog();
            return response;
        }
    }

    private static void timeMark1(){
        timer.start(timerMark1);
    }
    private static void timeMark2(){
        timer.start(timerMark1);
    }
    private static void timeMarkPrintLog(){
        logger.debug("{}耗时{} ，{} 耗时 {}",timerMark1, timer.intervalPretty(timerMark1),timerMark2,timer.intervalPretty(timerMark2));
        timer.clear();
    }

    //TODO 完成指定上传者的动态监测，识别后增加任务进行弹幕发送，同时规划一个区分进度的断点检测
    /**
     * 发送弹幕信息
     * https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/danmaku/action.md
     * @param cid       视频cid
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
    public static HttpResponse<String> sendDanMu(long cid, @NotNull String msg, String bvId, int avId, Long progress, Integer color, Float fontSize, Integer pool, Integer mode,String imgKey,String subKey, String cookie, String accessKey) throws URISyntaxException, IOException, InterruptedException {
        timeMark1();
        HttpClient httpClient = LinkUtil.getNormalHttpClient();
        Map<String, Object> postDataMap = new LinkedHashMap<>();
        //视频弹幕
        postDataMap.put("type", 1);
        postDataMap.put("oid", cid);
        postDataMap.put("msg", msg);
        postDataMap.put("rnd", System.currentTimeMillis()+"000000");
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
                TreeMap<String, Object> urlParamMap = new TreeMap<>();
                long wts = System.currentTimeMillis() / 1000;
                urlParamMap.put("csrf", csrf);
                urlParamMap.put("wts", wts);
                String param = urlParamMap.entrySet().stream()
                        .map(it -> String.format("%s=%s", it.getKey(), BiliInfoUtil.encodeURIComponent(it.getValue())))
                        .collect(Collectors.joining("&"));
                String wRid = BiliInfoUtil.createWRid(wts, param, imgKey, subKey);
                String url = DANMU_API +"?"+ param + "&w_rid=" + wRid;
                timeMark2();
                httpRequest = HttpRequest.newBuilder(new URI(url))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("cookie",cookie)
                        .POST(HttpRequest.BodyPublishers.ofString(FormatUtil.encodePostStringByMap(postDataMap)))
                        .build();

            } else {
                throw new IllegalArgumentException("传入的CK格式不正确，无法找到bili_jct(csrf)");
            }
        } else {
            postDataMap.put("access_key", accessKey	);
            timeMark2();
            httpRequest = HttpRequest.newBuilder(new URI(DANMU_API))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(FormatUtil.encodePostStringByMap(postDataMap)))
                    .build();
        }
        HttpResponse<String> send = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        timeMarkPrintLog();
        return send;
    }

}
