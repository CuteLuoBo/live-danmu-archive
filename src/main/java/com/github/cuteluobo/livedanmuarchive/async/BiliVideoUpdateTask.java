package com.github.cuteluobo.livedanmuarchive.async;

import com.github.cuteluobo.livedanmuarchive.enums.danmu.send.VideoPlatform;
import com.github.cuteluobo.livedanmuarchive.model.DanmuAccountTaskModel;
import com.github.cuteluobo.livedanmuarchive.model.DanmuSenderTaskModel;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.DynamicVideoData;
import com.github.cuteluobo.livedanmuarchive.service.database.MainDatabaseService;
import com.github.cuteluobo.livedanmuarchive.utils.BiliInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * B站的视频更新监听任务
 * @author CuteLuoBo
 * @date 2023/3/16 13:17
 */
public class BiliVideoUpdateTask {
    private final Logger logger = LoggerFactory.getLogger(BiliVideoUpdateTask.class);
    private String uid;
    private DanmuSenderTaskModel latest;

    public BiliVideoUpdateTask(String uid) {
        this.uid = uid;
    }


    /**
     * 更新视频任务
     * @return 创建的任务
     */
    public Runnable updateLatestVideoId() {
        /*
         * 1.从主数据库中，读取最新用户的视频弹幕任务储存，以最新视频ID为基础，通过调整API的偏移数不断遍历并保存，到一开始数据库中最新的视频ID
         * (略过，此步交给弹幕发送任务时处理)解析时，使用配置文件的标题和tags匹配，不符合要求时，在数据模型的skip标志为跳过
         * 3.数据库没有记录时，只解析并储存最新的视频ID结果
         * */
        return () -> {
            MainDatabaseService mainDatabaseService = MainDatabaseService.getInstance();
            DanmuSenderTaskModel databaseLatest = mainDatabaseService.getLatestOneByCreatorUid(uid);
            if (databaseLatest != null) {
                latest = databaseLatest;
            }
            List<String> totalAddList = new ArrayList<>();
            try {
                DynamicVideoData data = BiliInfoUtil.getDynamicVideoList(Long.parseLong(uid), 0);
                if (data == null) {
                    logger.error("尝试获取 {} 用户视频动态，无返回结果", uid);
                    return;
                }
                List<Map.Entry<String, Long>> videoList = data.getVideoList();
                if (latest == null) {
                    //没有记录时，取最新的一个视频添加
                    if (videoList.size() > 0) {
                        Map.Entry<String, Long> videoData = videoList.get(0);
                        DanmuSenderTaskModel danmuSenderTaskModel = new DanmuSenderTaskModel(VideoPlatform.BILIBILI.getName(), uid, videoData.getKey(), videoData.getValue());
                        mainDatabaseService.addSenderTask(danmuSenderTaskModel);
                        totalAddList.add(danmuSenderTaskModel.getVideoId());
                    }
                } else {
                    //最新获取的列表中没有最后的BV号时，循环添加
                    String lastBv = latest.getVideoId();
                    do {
                        videoList = data.getVideoList();
                        videoList.stream()
                                .filter(en -> en.getValue() > latest.getVideoCreatedTime() && !lastBv.equals(en.getKey()))
                                .forEach(videoData -> {
                                    DanmuSenderTaskModel danmuSenderTaskModel = new DanmuSenderTaskModel(VideoPlatform.BILIBILI.getName(), uid, videoData.getKey(), videoData.getValue());
                                    mainDatabaseService.addSenderTask(danmuSenderTaskModel);
                                    totalAddList.add(danmuSenderTaskModel.getVideoId());
                                });
                        data = BiliInfoUtil.getDynamicVideoList(Long.parseLong(uid), data.getOffsetId());
                        if (data == null) {
                            break;
                        }
                    } while (videoList.stream().noneMatch(en -> lastBv.equals(en.getKey())));
                }
                logger.info("uid:{}，UP视频监听任务,本次添加的视频数量{}，列表：{}", uid, totalAddList.size(), String.join(",", totalAddList));
            } catch (Exception e) {
                logger.error("获取 {} 用户视频动态时发生错误", uid, e);
            }
        };
    }
}
