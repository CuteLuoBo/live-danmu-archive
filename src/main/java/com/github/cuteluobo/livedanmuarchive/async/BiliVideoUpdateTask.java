package com.github.cuteluobo.livedanmuarchive.async;

import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendTaskField;
import com.github.cuteluobo.livedanmuarchive.enums.danmu.send.VideoPlatform;
import com.github.cuteluobo.livedanmuarchive.exception.AuthInvalidException;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.model.DanmuSenderTaskModel;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseUserInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.DynamicVideoData;
import com.github.cuteluobo.livedanmuarchive.service.database.MainDatabaseService;
import com.github.cuteluobo.livedanmuarchive.utils.BiliInfoUtil;
import com.github.cuteluobo.livedanmuarchive.utils.BiliLoginUtil;
import com.github.cuteluobo.livedanmuarchive.utils.BiliVideoUtil;
import com.github.cuteluobo.livedanmuarchive.utils.CustomConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * B站的视频更新监听任务
 * @author CuteLuoBo
 * @date 2023/3/16 13:17
 */
public class BiliVideoUpdateTask extends VideoUpdateTask {
    private final Logger logger = LoggerFactory.getLogger(BiliVideoUpdateTask.class);
    private DanmuSenderTaskModel latest;
    private String cookie;
    private BaseUserInfo baseUserInfo;

    public BiliVideoUpdateTask(String uid,String cookie) {
        super(uid);
        loginCk(cookie);
    }

    public BiliVideoUpdateTask(String uid, String tagMatch, String titleMatch,String cookie) {
        super(uid, tagMatch, titleMatch);
        loginCk(cookie);
    }

    public BiliVideoUpdateTask(String uid, String tagMatch, String titleMatch, String videoPartTimeRegular, String videoPartTimeFormat,String cookie) {
        super(uid, tagMatch, titleMatch, videoPartTimeRegular, videoPartTimeFormat);
        loginCk(cookie);
    }

    private void loginCk(String cookie){
        this.cookie = cookie;
        baseUserInfo = BiliLoginUtil.getUserBaseInfoByCk(cookie);
    }

    /**
     * 更新视频任务
     * @return 创建的任务
     */
    @Override
    public Runnable updateLatestVideoId() {
        /*
         * 1.从主数据库中，读取最新用户的视频弹幕任务储存，以最新视频ID为基础，通过调整API的偏移数不断遍历并保存，到一开始数据库中最新的视频ID
         * 解析时，使用配置文件的标题和tags匹配，不符合要求时，在数据模型的skip标志为跳过
         * 3.数据库没有记录时，只解析并储存最新的视频ID结果
         * */
        return () -> {
            if (!baseUserInfo.isLogin()) {
                if (switchCookie()){
                    logger.info("切换账户CK成功，使用{}-{}请求数据",baseUserInfo.getUid(),baseUserInfo.getNickName());
                }else {
                    logger.error("切换账户CK失败，列表中的CK均失效");
                    return;
                }
            }
            MainDatabaseService mainDatabaseService = MainDatabaseService.getInstance();
            DanmuSenderTaskModel databaseLatest = mainDatabaseService.getLatestOneByCreatorUid(getUid());
            if (databaseLatest != null) {
                latest = databaseLatest;
            }
            List<String> totalAddList = new ArrayList<>();
            try {
                DynamicVideoData data = BiliInfoUtil.getDynamicVideoList(Long.parseLong(getUid()), 0,cookie);
                if (data == null) {
                    logger.error("尝试获取 {} 用户视频动态，无返回结果", getUid());
                    return;
                }
                List<Map.Entry<String, Long>> videoList = data.getVideoList();
                if (latest == null) {
                    //没有记录时，取最新的一个视频添加
                    if (!videoList.isEmpty()) {
                        Map.Entry<String, Long> videoData = videoList.get(0);
                        if (matchAndSave(videoData)) {
                            totalAddList.add(videoData.getKey());
                        }
                    }
                } else {
                    //最新获取的列表中没有最后的BV号时，循环添加
                    String lastBv = latest.getVideoId();
                    do {
                        videoList = data.getVideoList();
                        videoList.stream()
                                .filter(en -> en.getValue() > latest.getVideoCreatedTime() && !lastBv.equals(en.getKey()))
                                .forEach(videoData -> {
                                    if (matchAndSave(videoData)) {
                                        totalAddList.add(videoData.getKey());
                                    }
                                });
                        data = BiliInfoUtil.getDynamicVideoList(Long.parseLong(getUid()), data.getOffsetId(),cookie);
                        if (data == null) {
                            break;
                        }
                    } while (videoList.stream().noneMatch(en -> lastBv.equals(en.getKey())));
                }
                logger.info("uid:{}，UP视频监听任务,本次添加的视频数量{}，列表：{}", getUid(), totalAddList.size(), String.join(",", totalAddList));
            }
            catch (ServiceException serviceException) {
                logger.error("获取 {} 用户视频动态时发生错误", getUid(), serviceException);
                logger.trace("详细异常信息:", serviceException.getOriginalException());
            }
            catch (AuthInvalidException authInvalidException) {
                logger.error("获取 {} 用户视频动态时发生错误，请求账户失效", getUid(), authInvalidException);
                baseUserInfo.setLogin(false);
            }
            catch (Exception e) {
                logger.error("获取 {} 用户视频动态时发生错误", getUid(), e);
            }
        };
    }

    private boolean switchCookie(){
        List<String> strings = CustomConfigUtil.getSenderCookieList();
        for (String ck : strings){
            BaseUserInfo baseUserInfoTemp = BiliLoginUtil.getUserBaseInfoByCk(ck);
            if (baseUserInfoTemp.isLogin()){
                this.baseUserInfo = baseUserInfoTemp;
                this.cookie = ck;
                return true;
            }
        }
        return false;
    }

    /**
     * 匹配和保存视频信息
     * @param videoData 视频信息
     * @return 匹配结果
     */
    private boolean matchAndSave(Map.Entry<String, Long> videoData){
        DanmuSenderTaskModel danmuSenderTaskModel = new DanmuSenderTaskModel(VideoPlatform.BILIBILI.getName(), getUid(), videoData.getKey(), videoData.getValue());
        //匹配标题和tags后再添加
        try {
            BiliVideoUtil.matchVideo(videoData.getKey(), getVideoPartTimeRegular(), getVideoPartTimeFormat(), getTitleMatch(), getTagMatch());
        } catch (ServiceException serviceException) {
            logger.info("{} 视频未通过匹配，设置为跳过，具体结果：{}", videoData.getKey(), serviceException.getLocalizedMessage());
            logger.trace("详细异常信息:", serviceException.getOriginalException());
            //解析错误时，设置视频稿件跳过
            danmuSenderTaskModel.setSkip(true);
            MainDatabaseService.getInstance().addSenderTask(danmuSenderTaskModel);
            return false;
        }
        MainDatabaseService.getInstance().addSenderTask(danmuSenderTaskModel);
        return true;
    }
}
