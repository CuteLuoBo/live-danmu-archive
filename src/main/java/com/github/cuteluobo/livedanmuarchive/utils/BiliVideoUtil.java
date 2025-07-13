package com.github.cuteluobo.livedanmuarchive.utils;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseResult;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoAllInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoPage;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoPageData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedPartVideoData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * B站视频操作相关接口
 * @author CuteLuoBo
 * @date 2022/12/28 12:31
 */
public class BiliVideoUtil {
    private final static Logger logger = LoggerFactory.getLogger(BiliVideoUtil.class);
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
        String url = VIDEO_INFO_API + (bvId != null ? "?bvid=" + bvId : "?aid=" + avId);
        Map<String,Object> headerMap = new HashMap<>();
        if (sessData != null) {
            headerMap.put("cookie", sessData);
        }
        //请求解析
        String bodyString = NettyLinkUtil.getHtmlBodyByNetty(url,headerMap);
        VideoAllInfo videoAllInfo = new VideoAllInfo();
        ObjectMapper objectMapper = new ObjectMapper();
        BaseResult<VideoAllInfo> baseResult = new BaseResult<>();

        JsonNode body = objectMapper.readTree(bodyString);
        parseBaseResult(body, baseResult);
        baseResult.setData(videoAllInfo);
        if (BaseResult.OK_CODE == body.get("code").asInt()) {

        JsonNode data = body.get("data");
        JsonNode view = data.get("View");
        //基础视频数据
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
        //视频创建者数据
        JsonNode mainCard = data.get("Card");
        JsonNode card = mainCard.get("card");
        videoAllInfo.setCreatorUid(card.get("mid").asText());
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
        String url = VIDEO_PAGE_LIST_API + (bvId != null ? "?bvid=" + bvId : "?aid=" + avId);
        String bodyString = NettyLinkUtil.getHtmlBodyByNetty(url,null);
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

    /**
     * 判断视频（标题/tag）是否符合配置，并封装转换后的视频数据
     * @param videoId 视频ID
     * @param matchTitle 需要匹配的标题，为Null时跳过匹配
     * @param tagsString 需要匹配的tags字符串，为Null时跳过匹配
     * @return 解析结果
     * @throws ServiceException 解析时错误，已封装好错误信息和原始错误（如有）
     */
    public static BiliProcessedVideoData matchVideo(@NotNull String videoId, @NotNull String timeRegular, @NotNull String partTimeFormat, String matchTitle, String tagsString) throws ServiceException {
        BiliProcessedVideoData processedVideoData = new BiliProcessedVideoData();
        List<VideoPage> videoPageList;
        VideoAllInfo videoAllInfo;
        try {
            //调用网络请求获取视频信息
            BaseResult<VideoAllInfo> videoAllInfoBaseResult = BiliVideoUtil.getVideoAllInfo(videoId, null, null);
            videoAllInfo = videoAllInfoBaseResult.getData();
            if (videoAllInfo.getBvId() == null) {
                throw new ServiceException("ID对应视频不存在");
            }
            //解析数据
            processedVideoData.setVideoName(videoAllInfo.getTitle());
            processedVideoData.setBvId(videoAllInfo.getBvId());
            processedVideoData.setCreatorUid(videoAllInfo.getCreatorUid());
            //时间戳，秒级转毫秒级
            processedVideoData.setCreateTime(videoAllInfo.getPubDate() * 1000L);
            videoPageList = videoAllInfo.getPages();
            //匹配视频标题
            if (matchTitle != null && matchTitle.trim().length() > 0) {
                if (!videoAllInfo.getTitle().contains(matchTitle)) {
                    throw new ServiceException(String.format("视频%s，标题：%s，不含匹配标题字符:%s", videoAllInfo.getBvId(), videoAllInfo.getTitle(), matchTitle));
                }
            }
            //匹配视频TAG
            if (tagsString != null && tagsString.trim().length() > 0) {
                String[] tags = tagsString.split(",");
                List<String> videoTagsList = videoAllInfo.getTagList();
                if (!videoTagsList.containsAll(Arrays.asList(tags))) {
                    throw new ServiceException(String.format("视频%s，tag列表：%s，不完全匹配配置tag列表:%s", videoAllInfo.getBvId(), String.join(",", videoTagsList), tagsString));
                }
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new ServiceException(String.format("获取%s视频详细分P数据失败", videoId),e);
        }
        Pattern timePattern = Pattern.compile(timeRegular);
        DateTimeFormatter dateTimeFormatter;
        try{
            dateTimeFormatter = DateTimeFormatter.ofPattern(partTimeFormat);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException(String.format("无法识别传入的时间解析格式\"%s\"，请及时调整！当前任务将跳过", partTimeFormat),illegalArgumentException);
        }
        List<BiliProcessedPartVideoData> partVideoDataList = new ArrayList<>(videoPageList.size());
        for (VideoPage videoPage : videoPageList
        ) {
            BiliProcessedPartVideoData partData = new BiliProcessedPartVideoData();
            //解析分P数据
            partData.setBvId(videoAllInfo.getBvId());
            partData.setCid(videoPage.getCid());
            long duration = videoPage.getDuration();
            partData.setDuration(duration);
            String partName = videoPage.getPartName();
            partData.setPartName(partName);
            //解析分P开始时间
            Matcher matcher = timePattern.matcher(partName);
            if (matcher.find()) {
                String timeString = matcher.group(1);
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(timeString, dateTimeFormatter);
                    long videoStartMillTime = dateTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
                    partData.setVideoStartMillTime(videoStartMillTime);
                    partData.setVideoEndMillTime(videoStartMillTime + duration * 1000);
                    partVideoDataList.add(partData);
                } catch (DateTimeParseException dateTimeParseException) {
                    logger.warn("视频{}分P标题{}无法解析时间，跳过", videoId, partName);
                }
            } else {
                logger.warn("视频{}分P标题{}正则未识别到时间，跳过", videoId, partName);
            }
        }
        processedVideoData.setPartVideoDataList(partVideoDataList);
        return processedVideoData;
    }
}
