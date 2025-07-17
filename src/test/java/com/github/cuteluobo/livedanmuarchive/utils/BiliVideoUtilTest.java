package com.github.cuteluobo.livedanmuarchive.utils;

import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendTaskField;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseResult;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoAllInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoPage;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.VideoPageData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BiliVideoUtilTest {
    public static String  bvid = "BV1AJ32zjEEN";
    @DisplayName("测试获取视频分页信息")
    @Test
    public void getVideoPageData() throws URISyntaxException, IOException, InterruptedException {
        int avid = 170001;
        VideoPageData av = BiliVideoUtil.getVideoPageData(null, avid);
        checkVideoPage(av,true);

        VideoPageData bv = BiliVideoUtil.getVideoPageData(bvid, null);
        checkVideoPage(bv,true);

        VideoPageData error = BiliVideoUtil.getVideoPageData("BV1", null);
        checkVideoPage(error,false);
    }

    @DisplayName("测试获取视频详细信息")
    @Test
    public void getVideoAllInfo() throws URISyntaxException, IOException, InterruptedException {
        int avid = 170001;
        BaseResult<VideoAllInfo> av = BiliVideoUtil.getVideoAllInfo(null, avid,null);
        assertNotNull(av);
        checkVideoInfo(av,true);

        BaseResult<VideoAllInfo> bv = BiliVideoUtil.getVideoAllInfo(bvid, null,null);
        checkVideoInfo(bv,true);

        BaseResult<VideoAllInfo> error = BiliVideoUtil.getVideoAllInfo("BV1", null,null);
        checkVideoInfo(error,false);
    }
    private void checkVideoInfo(BaseResult<VideoAllInfo> baseResult, boolean right) {
        System.out.println("==============");
        assertNotNull(baseResult);
        if (right) {
            VideoAllInfo videoAllInfo = baseResult.getData();
            assertNotNull(videoAllInfo);
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder
                    .append("《").append(videoAllInfo.getTitle()).append("》 ( ").append("av").append(videoAllInfo.getAvId()).append("/").append(videoAllInfo.getBvId()).append(" ) ").append("\r\n'")
                    .append("视频数： ").append(videoAllInfo.getVideos()).append("\r\n")
                    .append("tags： ").append(String.join(",", videoAllInfo.getTagList()))
            ;
            System.out.println(stringBuilder.toString());
        } else {
            assertNotEquals(BaseResult.OK_CODE,baseResult.getCode());
        }

    }

    private void checkVideoPage(VideoPageData videoPageData, boolean right) {
        System.out.println("==============");
        assertNotNull(videoPageData);
        if (right) {
            assertEquals(VideoPageData.OK_CODE,videoPageData.getCode());
            List<VideoPage> pageList = videoPageData.getData();
            assertNotNull(pageList);
            pageList.stream().map(VideoPage::getPartName).forEach(System.out::println);
        } else {
            assertNotEquals(VideoPageData.OK_CODE,videoPageData.getCode());
        }
    }
    @Test
    void testMatchVideo() throws ServiceException {
        BiliProcessedVideoData processedVideoData = BiliVideoUtil
                .matchVideo(bvid,
                        ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getNormalValue(),
                        ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getNormalValue(),
                        "【甜药】", "甜药");
        assertThrows(ServiceException.class, () ->{
            BiliVideoUtil
                    .matchVideo(bvid,
                            ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getNormalValue(),
                            ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getNormalValue(),
                            "[ErrorTitle]", null);
        });
        assertThrows(ServiceException.class, () ->{
            BiliVideoUtil
                    .matchVideo(bvid,
                            ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getNormalValue(),
                            ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getNormalValue(),
                            null, "error,tags");
        });
        System.out.println(processedVideoData);
    }
}