package com.github.cuteluobo.livedanmuarchive.utils;

import com.github.cuteluobo.livedanmuarchive.command.base.MyThreadFactory;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * 连接/通信相关工具类
 *
 * @author CuteLuoBo
 * @date 2022/11/16 17:26
 */
public class LinkUtil {
    static Logger logger = LoggerFactory.getLogger(LinkUtil.class);

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(5, 20, 30, TimeUnit.SECONDS
            , new SynchronousQueue<>(true)
            , new MyThreadFactory("LinkUtil"));
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(60)).version(HttpClient.Version.HTTP_1_1).executor(THREAD_POOL_EXECUTOR).build();
    public static final Map<String, String> NORMAL_HEADER = new HashMap<>(){{
        put("user-agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML," +
                "like Gecko) Chrome/79.0.3945.88 Mobile Safari/537.36");
    }};
    /**
     * 获取默认HTTP客户端
     * @return HTTP客户端
     */
    public static HttpClient getNormalHttpClient()  {
        return HTTP_CLIENT;
    }

    /**
     * 获取默认封装的Http请求
     * @param uri 访问的网址URI
     * @return 默认封装的Http请求
     */
    public static HttpRequest getNormalHttpRequest(URI uri) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri).GET();
        for (Map.Entry<String, String> entry :
                NORMAL_HEADER.entrySet()) {
            requestBuilder.setHeader(entry.getKey(), entry.getValue());
        }
        return requestBuilder.build();
    }

    /**
     * 获取网页主体数据
     * @param url   访问的URL网址
     * @param headerMap 指定的请求头Map
     * @return 网址解析的String
     */
    public static String getHtmlBody(String url, Map<String, String> headerMap) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(new URI(url)).GET();
        for (Map.Entry<String, String> entry :
                headerMap.entrySet()) {
            requestBuilder.setHeader(entry.getKey(), entry.getValue());
        }
        //获取返回数据
        HttpResponse<InputStream> httpResponse = HTTP_CLIENT.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
        String body;
        //验证是否存在gzip压缩，参考https://golb.hplar.ch/2019/01/java-11-http-client.html --- Compression章节
        String encoding = httpResponse.headers().firstValue("Content-Encoding").orElse("");
        if ("gzip".equals(encoding)) {
            logger.debug("请求页面使用gzip压缩");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (InputStream is = new GZIPInputStream(httpResponse.body()); var autoCloseOs = os) {
                is.transferTo(autoCloseOs);
            }
            body = os.toString(StandardCharsets.UTF_8);
        }else{
            logger.debug("请求页面未压缩");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (var is = httpResponse.body(); var autoCloseOs = os) {
                is.transferTo(autoCloseOs);
            }
            body = os.toString(StandardCharsets.UTF_8);
        }
        return body;
    }

    /**
     * 获取网页主体数据，使用默认请求头
     * @param url   访问的URL网址
     * @return 网址解析的String
     */
    public static String getHtmlBody(String url) throws URISyntaxException, IOException, InterruptedException {
        return getHtmlBody(url, NORMAL_HEADER);
    }
}
