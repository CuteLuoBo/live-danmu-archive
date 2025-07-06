package com.github.cuteluobo.livedanmuarchive.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.DynamicVideoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * B站信息获取相关
 * @author CuteLuoBo
 * @date 2023/1/9 15:25
 */
public class BiliInfoUtil {
    static Logger logger = LoggerFactory.getLogger(BiliInfoUtil.class);
    public static final String DYNAMIC_URL = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history";
    public static final Pattern TITLE_PATTERN = Pattern.compile("\"title\\\\\":\\\\\"([^\\\\\",]*)\\\\\",");
    public static final Pattern DESC_PATTERN = Pattern.compile("\"desc\\\\\":\\\\\"([^\\\\\",]*)\\\\\",");
    //解析直播间信息流https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/live/message_stream.md
    public static final String DanmuInfo_URL = "https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo";
    /**
     * 获取指定用户的视频动态消息，默认获取12条
     *
     * API来源：https://github.com/SocialSisterYi/bilibili-API-collect/issues/361
     * @param uid    B站用户UID
     * @param offset 偏移动态ID，为0时表示从最新取
     * @return  处理后获得的前12个动态的视频BV号
     * @throws URISyntaxException URI错误
     * @throws IOException  网络IO错误
     * @throws InterruptedException 线程中断
     * @throws ServiceException 解析用户动态数据时出现错误
     */
    public static DynamicVideoData getDynamicVideoList(long uid,long offset,String cookie) throws URISyntaxException, IOException, InterruptedException, ServiceException {
        //构建请求
        HttpClient httpClient = LinkUtil.getNormalHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder(new URI(DYNAMIC_URL+"?" + "host_uid="+uid+"&offset_dynamic_id="+offset))
                .header("cookie",cookie)
                .GET()
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String bodyString = httpResponse.body();

        List<Map.Entry<String,Long>> videoList = new ArrayList<>(12);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode body = objectMapper.readTree(bodyString);
            int code = body.get("code").intValue();
            if (code == 0) {
                logger.trace("获取用户动态列表：请求用户uid：{}，动态ID偏移:{}，返回结果：{}",uid,offset,bodyString);
                JsonNode data = body.get("data");
                JsonNode cards = data.withArray("cards");
                DynamicVideoData dynamicVideoData = new DynamicVideoData(data.get("next_offset").asLong());
                Iterator<JsonNode> iterator = cards.elements();
                while (iterator.hasNext()) {
                    JsonNode card = iterator.next();
                    JsonNode desc = card.get("desc");
                    JsonNode type = desc.get("type");
                    //视频动态==8
                    if (type.asInt() == 8) {
                        JsonNode bvNode = desc.get("bvid");
                        String bvString = bvNode.asText();
                        if (bvString.length() > 0) {
                            videoList.add(new AbstractMap.SimpleEntry<>(bvString, desc.get("timestamp").asInt() * 1000L));
                            JsonNode cardInfo = card.get("card");
                            String cardInfoString = cardInfo.asText();
                            Matcher titleMatcher = TITLE_PATTERN.matcher(cardInfoString);
                            Matcher descMatcher = DESC_PATTERN.matcher(cardInfoString);
                            String title = null;
                            String descString = null;
                            if (titleMatcher.find()) {
                                title = titleMatcher.group(1);
                            }
                            if (descMatcher.find()) {
                                descString = descMatcher.group(1);
                            }
                            logger.trace("{}.标题：{}，描述,{},bvId:{}",videoList.size()+1,title,descString,bvString);
                        }
                    }
                }
                dynamicVideoData.setVideoList(videoList);
                return dynamicVideoData;
            } else {
                logger.warn("获取用户动态失败，请求用户uid：{}，动态ID偏移:{}，返回结果：{}",uid,offset,bodyString);
                return null;
            }
        } catch (Exception e) {
            throw new ServiceException("解析用户动态数据时出现错误,"+e.getMessage(), e);
        }
    }
}
