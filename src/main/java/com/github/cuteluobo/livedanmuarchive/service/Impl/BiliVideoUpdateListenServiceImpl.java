package com.github.cuteluobo.livedanmuarchive.service.Impl;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.StrUtil;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;
import com.github.cuteluobo.livedanmuarchive.async.BiliVideoUpdateTask;
import com.github.cuteluobo.livedanmuarchive.async.VideoUpdateTask;
import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendAccountField;
import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendTaskField;
import com.github.cuteluobo.livedanmuarchive.enums.danmu.send.VideoPlatform;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.model.DanmuSenderTaskModel;
import com.github.cuteluobo.livedanmuarchive.pojo.BiliDanMuSenderAccountData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import com.github.cuteluobo.livedanmuarchive.service.VideoUpdateListenService;
import com.github.cuteluobo.livedanmuarchive.service.database.MainDatabaseService;
import com.github.cuteluobo.livedanmuarchive.utils.BiliVideoUtil;
import com.github.cuteluobo.livedanmuarchive.utils.CustomConfigUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * B站的视频更新监听实现类
 * @author CuteLuoBo
 * @date 2023/3/16 10:57
 */
public class BiliVideoUpdateListenServiceImpl implements VideoUpdateListenService {

    private static final Logger logger = LoggerFactory.getLogger(BiliVideoUpdateListenServiceImpl.class);

    private String cookie = "";
    /**
     * 监听的延迟时间(秒)
     */
    private int delaySeconds = 60;

    private Map<String, ScheduledFuture<?>> taskMap = new HashMap<>();

    /**
     * 延时执行线程
     */
//    private ExecutorService pool = new ThreadPoolExecutor(1, 1024, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), new NamedThreadFactory("B站视频动态监听接口", false));

    private BiliVideoUpdateListenServiceImpl() {
        //读取配置文件
        CustomConfigUtil customConfigUtil = CustomConfigUtil.INSTANCE;
        YamlMapping allConfig = customConfigUtil.getConfigMapping();
        YamlMapping taskMainConfig = allConfig.yamlMapping(ConfigDanMuAutoSendTaskField.MAIN_FIELD.getFieldString());
        //获取监听延迟时间,默认60秒
        int delaySecondsTemp = taskMainConfig.integer(ConfigDanMuAutoSendTaskField.LISTEN_DELAY_TIME.getFieldString());
        if (delaySecondsTemp > 0) {
            delaySeconds = delaySecondsTemp;
        }
        cookie = CustomConfigUtil.getSenderCookie();
    }

    private static class InstanceClass {
        private final static BiliVideoUpdateListenServiceImpl INSTANCE = new BiliVideoUpdateListenServiceImpl();
    }

    public static BiliVideoUpdateListenServiceImpl getInstance() {
        return InstanceClass.INSTANCE;
    }



    private ScheduledExecutorService pool = Executors.newScheduledThreadPool(1, new NamedThreadFactory("B站视频动态监听接口", false));
    /**
     * 开启针对某个特定用户的（动态）视频更新信息监听
     *
     * @param userId 用户ID
     * @return 是否启用成功
     */
    @Override
    public boolean startVideoUpdateListen(@NotNull String userId) {
        if (userId.trim().length() == 0) {
            logger.error("传入UID为空");
            return false;
        }
        ScheduledFuture<?> future = taskMap.get(userId);
        //如果已有任务存活，略过提交
        if (future != null && !future.isCancelled()) {
            logger.info("当前UID已有正在执行的监听任务！此次提交将跳过");
            return false;
        }
        BiliVideoUpdateTask task = new BiliVideoUpdateTask(userId,cookie);
        future = pool.scheduleWithFixedDelay(task.updateLatestVideoId(), 0, delaySeconds, TimeUnit.SECONDS);
        taskMap.put(userId, future);
        return true;
    }

    /**
     * 传入视频监听任务
     * @param task 创建好的任务
     * @return 创建结果
     */
    @Override
    public boolean startVideoUpdateListen(VideoUpdateTask task) {
        if (task == null) {
            logger.error("传入任务为空");
            return false;
        }
        ScheduledFuture<?> future = taskMap.get(task.getUid());
        //如果已有任务存活，略过提交
        if (future != null && !future.isCancelled()) {
            logger.info("当前UID已有正在执行的监听任务！此次提交将跳过");
            return false;
        }
        future = pool.scheduleWithFixedDelay(task.updateLatestVideoId(), 0, delaySeconds, TimeUnit.SECONDS);
        taskMap.put(task.getUid(), future);
        return true;
    }

    /**
     * 手动添加视频的ID信息，一般由用户通过直接命令添加
     *
     * @param videoId 视频ID
     * @return 是否添加成功
     */
    @Override
    public boolean addVideo(String videoId) {
        YamlMapping configMapping = CustomConfigUtil.INSTANCE.getConfigMapping();
        YamlMapping taskMapping = configMapping.yamlMapping(ConfigDanMuAutoSendTaskField.MAIN_FIELD.getFieldString());
        try {
            //匹配视频
            BiliProcessedVideoData biliProcessedVideoData = BiliVideoUtil.matchVideo(videoId,
                    taskMapping.string(ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getFieldString()),
                    taskMapping.string(ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getFieldString()),
                    null,
                    null);
            DanmuSenderTaskModel danmuSenderTaskModel = new DanmuSenderTaskModel(VideoPlatform.BILIBILI.getName(),
                    biliProcessedVideoData.getCreatorUid(),
                    biliProcessedVideoData.getBvId(),
                    biliProcessedVideoData.getCreateTime());
            MainDatabaseService.getInstance().addSenderTask(danmuSenderTaskModel);
            logger.info("添加{}视频任务成功", videoId);
            return true;
        }catch (ServiceException e) {
            logger.error("添加{}视频任务失败,{}", videoId,e.getMessage(),e.getOriginalException());
        }
        return false;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
