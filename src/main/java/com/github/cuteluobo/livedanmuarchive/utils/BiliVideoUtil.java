package com.github.cuteluobo.livedanmuarchive.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseResult;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoAllInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoPage;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoPageData;

import javax.json.Json;
import javax.json.JsonArray;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * B站视频操作相关接口
 * @author CuteLuoBo
 * @date 2022/12/28 12:31
 */
public class BiliVideoUtil {
    /**
     * 视频分P查询接口
     */
    public static final String VIDEO_PAGE_LIST_API = "http://api.bilibili.com/x/player/pagelist";

    public static final String VIDEO_INFO_API = "http://api.bilibili.com/x/web-interface/view/detail";

    /**
     * 获取视频详细信息
     * @param bvId     BV，择一
     * @param avId     AV，择一
     * @param sessData  cookie，可选
     * @return 获取结果
     * @throws URISyntaxException URI初始化失败
     * @throws IOException 网络ID错误
     * @throws InterruptedException 线程中断
     */
    public static BaseResult<VideoAllInfo> getVideoAllInfo(String bvId, Integer avId, String sessData) throws URISyntaxException, IOException, InterruptedException {
        //构建请求
        HttpClient httpClient = LinkUtil.getNormalHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder(new URI(VIDEO_INFO_API + (bvId != null ? "?bvid=" + bvId : "?aid=" + avId)))
                .GET()
                .header("cookie", sessData == null ? "" : sessData)
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        //请求解析
        String bodyString = httpResponse.body();
        VideoAllInfo videoAllInfo = new VideoAllInfo();
        ObjectMapper objectMapper = new ObjectMapper();
        BaseResult<VideoAllInfo> baseResult = new BaseResult<>();

        JsonNode body = objectMapper.readTree(bodyString);
        parseBaseResult(body, baseResult);
        baseResult.setData(videoAllInfo);
        if (BaseResult.OK_CODE == body.get("code").asInt()) {

        JsonNode data = body.get("data");
        JsonNode view = data.get("View");
        videoAllInfo.setAvId(view.get("aid").asInt());
        videoAllInfo.setBvId(view.get("bvid").asText());
        videoAllInfo.setVideos(view.get("videos").asInt());
        videoAllInfo.setTid(view.get("tid").asInt());
        videoAllInfo.settName(view.get("tname").asText());
        videoAllInfo.setCopyright(view.get("copyright").asInt());
        videoAllInfo.setPic(view.get("pic").asText());
        videoAllInfo.setTitle(view.get("title").asText());
        videoAllInfo.setPubDate(view.get("pubdate").asInt());
        videoAllInfo.setCtime(view.get("ctime").asInt());
        videoAllInfo.setDesc(view.get("desc").asText());
//        videoAllInfo.setDescV2(data.get("desc_v2"));
        videoAllInfo.setState(view.get("state").asInt());
        videoAllInfo.setDuration(view.get("duration").asInt());
        //分P数据
        ArrayNode pageData = view.withArray("pages");
        List<VideoPage> videoPageList = new ArrayList<>();
        Iterator<JsonNode> pageDataIterator = pageData.elements();
        while (pageDataIterator.hasNext()) {
            JsonNode pageNode = pageDataIterator.next();
            videoPageList.add(parseVideoPage(pageNode));
        }
        videoAllInfo.setPages(videoPageList);
        //tag标签
        List<String> tags = new ArrayList<>();
        ArrayNode tagsData = data.withArray("Tags");
        Iterator<JsonNode> tagsIterator = tagsData.elements();
        while (tagsIterator.hasNext()) {
            JsonNode node = tagsIterator.next();
            tags.add(node.get("tag_name").asText());
        }
        videoAllInfo.setTagList(tags);
        }
        return baseResult;
    }

    /**
     * 获取视频详细分P数据
     * @param bvId BV ID,择一
     * @param avId AV ID,择一
     * @return 查询后结果
     * @throws URISyntaxException URI初始化失败
     * @throws IOException 网络ID错误
     * @throws InterruptedException 线程中断
     */
    public static VideoPageData getVideoPageData(String bvId, Integer avId) throws URISyntaxException, IOException, InterruptedException {
        //构建请求
        HttpClient httpClient = LinkUtil.getNormalHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder(new URI(VIDEO_PAGE_LIST_API + (bvId != null ? "?bvid=" + bvId : "?aid=" + avId)))
                .GET()
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        //请求解析
        String bodyString = httpResponse.body();
        VideoPageData videoPageData = new VideoPageData();
        List<VideoPage> videoPageList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode body = objectMapper.readTree(bodyString);
        parseBaseResult(body, videoPageData);
        videoPageData.setData(videoPageList);
        //分P数据
        ArrayNode pageData = body.withArray("data");
        Iterator<JsonNode> pageDataIterator = pageData.elements();
        while (pageDataIterator.hasNext()) {
            JsonNode pageNode = pageDataIterator.next();
            videoPageList.add(parseVideoPage(pageNode));
        }
        return videoPageData;
    }

    private static VideoPage parseVideoPage(JsonNode jsonNode) {
        VideoPage videoPage = new VideoPage();
        videoPage.setCid(jsonNode.get("cid").asLong());
        videoPage.setPage(jsonNode.get("page").asInt());
        videoPage.setFrom(jsonNode.get("from").asText());
        videoPage.setPartName(jsonNode.get("part").asText());
        videoPage.setDuration(jsonNode.get("duration").asLong());
        //分辨率
        if (jsonNode.hasNonNull("dimension")) {
            JsonNode dimensionNode = jsonNode.get("dimension");
            videoPage.setWidth(dimensionNode.get("width").asInt());
            videoPage.setHeight(dimensionNode.get("height").asInt());
            videoPage.setRotate(dimensionNode.get("rotate").asBoolean());
        }
        return videoPage;
    }

    private static BaseResult<?> parseBaseResult(JsonNode jsonNode,BaseResult<?> baseResult) {
        baseResult.setCode(jsonNode.get("code").asInt());
        baseResult.setMessage(jsonNode.get("message").asText());
        baseResult.setTtl(jsonNode.get("ttl").asInt());
        return baseResult;
    }
}
