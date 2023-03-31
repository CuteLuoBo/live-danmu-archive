package com.github.cuteluobo.livedanmuarchive.pojo.biliapi;

import java.util.List;

/**
 * 视频分P列表数据
 * https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/video/info.md#%E6%9F%A5%E8%AF%A2%E8%A7%86%E9%A2%91%E5%88%86p%E5%88%97%E8%A1%A8-avidbvid%E8%BD%ACcid
 * @author CuteLuoBo
 * @date 2022/12/28 12:34
 */
public class VideoPageData extends BaseResult<List<VideoPage>> {
    public static int ERROR_CODE = -400;
    public static int VIDEO_NOT_FOUND_CODE = -404;
}
