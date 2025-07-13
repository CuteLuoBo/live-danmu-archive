package com.github.cuteluobo.livedanmuarchive.utils;

import cn.hutool.core.date.TimeInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 使用Netty相关的网络连接工具类
 *
 * @author: CuteLuoBo
 * @date: 2025/7/11  13:23
 * @version: 1.0.0
 */
public class NettyLinkUtil {
    static Logger logger = LoggerFactory.getLogger(NettyLinkUtil.class);
    static TimeInterval interval = new TimeInterval();

    public static final Map<String, Object> NORMAL_HEADER = new HashMap<>(){{
        put("user-agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML," +
                "like Gecko) Chrome/79.0.3945.88 Mobile Safari/537.36");
    }};
    public static HttpClient getNormalHttpClient(){
        HttpClient httpClient = HttpClient.create().followRedirect(true);
        return httpClient;
    }

    public static String getHtmlBodyByNetty(String url, Map<String, Object> headerMap) throws URISyntaxException, IOException, InterruptedException {
        return getHtmlBodyByNetty(url, headerMap, null);
    }

    public static String getHtmlBodyByNetty(String url,Map<String, Object> headerMap,Map<String,Object> postDataMap) throws URISyntaxException, IOException, InterruptedException {
        interval.start();
        reactor.netty.http.client.HttpClient httpClient = getNormalHttpClient();
        httpClient = httpClient.headers(headers -> {
            Optional.ofNullable(headerMap).orElse(NORMAL_HEADER).forEach(headers::set);
        });
        HttpClient.ResponseReceiver<?> responseReceiver;
        if (postDataMap == null) {
            responseReceiver = httpClient.get().uri(new URI(url));
        } else {
            responseReceiver = httpClient.post().uri(new URI( url)).send(ByteBufFlux.fromString(Mono.just(FormatUtil.encodePostStringByMap(postDataMap))));
        }
        String response = responseReceiver.responseContent().aggregate().asString(StandardCharsets.UTF_8).block();
        logger.info("请求耗时：{}ms",interval.intervalPretty());
        logger.debug("请求返回：{}",response);
        interval.clear();
        return response;
    }
}
