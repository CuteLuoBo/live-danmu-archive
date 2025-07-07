package com.github.cuteluobo.livedanmuarchive.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.StrUtil;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;
import com.github.cuteluobo.livedanmuarchive.async.BiliVideoUpdateTask;
import com.github.cuteluobo.livedanmuarchive.async.VideoUpdateTask;
import com.github.cuteluobo.livedanmuarchive.dto.DanMuSenderTaskSelector;
import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendAccountField;
import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendTaskField;
import com.github.cuteluobo.livedanmuarchive.enums.danmu.send.VideoPlatform;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.model.DanmuSenderTaskModel;
import com.github.cuteluobo.livedanmuarchive.pojo.BiliDanMuSenderAccountData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuSenderAccountData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import com.github.cuteluobo.livedanmuarchive.service.Impl.BiliVideoUpdateListenServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.persistence.BiliDanMuAutoSendServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.VideoUpdateListenService;
import com.github.cuteluobo.livedanmuarchive.service.database.MainDatabaseService;
import com.github.cuteluobo.livedanmuarchive.utils.BiliVideoUtil;
import com.github.cuteluobo.livedanmuarchive.utils.CustomConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 弹幕发送控制器
 * @author CuteLuoBo
 * @date 2023/3/13 13:37
 */
public class DanMuSenderController {

    private static final Logger logger = LoggerFactory.getLogger(DanMuSenderController.class);

    private BiliDanMuAutoSendServiceImpl danMuAutoSendService;

    private Map<String, Map<String, VideoUpdateTask>> platformListenMap;

    private ScheduledExecutorService timerPool = Executors.newScheduledThreadPool(2, new NamedThreadFactory("弹幕发送控制器定时线程",false));

    private boolean allowAutoPushQueue = true;
    private int queueNum = 5;
    private int delaySeconds = 30;
    private Queue<DanmuSenderTaskModel> danmuSenderTaskModelQueue = new LinkedBlockingDeque<>(queueNum);

    private String regular;
    private String timeFormat;

    private String link = "-";
    /**
     * 视频创建者UID与保存的文件名映射
     *  "视频平台-视频创建者UID"->保存的弹幕文件名
     */
    private Map<String, String> videoUidAndSaveFileNameMap = new HashMap<>();
    private VideoUpdateListenService biliVideoUpdateListenService = BiliVideoUpdateListenServiceImpl.getInstance();
    private DanMuSenderController() {
        initConfig();
        timerPool.scheduleWithFixedDelay(createQueuePushTask(), 0, delaySeconds, TimeUnit.SECONDS);
        timerPool.scheduleWithFixedDelay(createStartSendTask(), 10, delaySeconds, TimeUnit.SECONDS);
    }

    private static class InstanceClass{
        private final static DanMuSenderController INSTANCE = new DanMuSenderController();
    }

    public static DanMuSenderController getInstance() {
        return InstanceClass.INSTANCE;
    }

    private void initConfig() {
        logger.info("正在尝试从配置文件中获取弹幕发送相关数据...");
        //读取配置文件
        CustomConfigUtil customConfigUtil = CustomConfigUtil.INSTANCE;
        YamlMapping allConfig = customConfigUtil.getConfigMapping();
        YamlMapping taskMainConfig = allConfig.yamlMapping(ConfigDanMuAutoSendTaskField.MAIN_FIELD.getFieldString());
        //时间解析相关正则和格式
        regular =  taskMainConfig.string(ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_REGULAR.getFieldString());
        timeFormat = taskMainConfig.string(ConfigDanMuAutoSendTaskField.VIDEO_P_TIME_FORMAT.getFieldString());

        //读取账户
        logger.info("正在添加读取配置文件中弹幕发送账户数据...");
        YamlMapping accountMainConfig = allConfig.yamlMapping(ConfigDanMuAutoSendAccountField.MAIN_FIELD.getFieldString());
        YamlSequence accountList = accountMainConfig.yamlSequence(ConfigDanMuAutoSendAccountField.ACCOUNT_LIST.getFieldString());
        List<BiliDanMuSenderAccountData> accountDataList = new ArrayList<>();
        for (YamlNode node :
                accountList) {
            YamlMapping mapping = node.asMapping();
            BiliDanMuSenderAccountData biliDanMuSenderAccountData = new BiliDanMuSenderAccountData();
            biliDanMuSenderAccountData.setNickName(mapping.string(ConfigDanMuAutoSendAccountField.NICK_NAME.getFieldString()));
            biliDanMuSenderAccountData.setUserName(mapping.string(ConfigDanMuAutoSendAccountField.USER_NAME.getFieldString()));
            biliDanMuSenderAccountData.setPassword(mapping.string(ConfigDanMuAutoSendAccountField.PASSWORD.getFieldString()));
            biliDanMuSenderAccountData.setPassword(mapping.string(ConfigDanMuAutoSendAccountField.PASSWORD.getFieldString()));
            biliDanMuSenderAccountData.setCookies(mapping.string(ConfigDanMuAutoSendAccountField.COOKIES.getFieldString()));
            biliDanMuSenderAccountData.setAccessKey(mapping.string(ConfigDanMuAutoSendAccountField.ACCESS_KEY.getFieldString()));
            biliDanMuSenderAccountData.setAppKey(mapping.string(ConfigDanMuAutoSendAccountField.APP_KEY.getFieldString()));
            biliDanMuSenderAccountData.setAppSec(mapping.string(ConfigDanMuAutoSendAccountField.APP_SEC.getFieldString()));
            accountDataList.add(biliDanMuSenderAccountData);
        }
        danMuAutoSendService = BiliDanMuAutoSendServiceImpl.getInstance(accountDataList);

        //解析部署队列
        logger.info("正在解析部署队列...");
        YamlSequence deployListSequence = taskMainConfig.yamlSequence(ConfigDanMuAutoSendTaskField.DEPLOY_LIST.getFieldString());
        platformListenMap = new HashMap<>(VideoPlatform.values().length);
        Arrays.stream(VideoPlatform.values()).forEach(v ->{
            Map<String, VideoUpdateTask> listenIdTaskMap = platformListenMap.get(v.getName());
            if (listenIdTaskMap == null) {
                listenIdTaskMap = new LinkedHashMap<>();
            }
            for (Iterator<YamlNode> it = deployListSequence.iterator(); it.hasNext(); ) {
                YamlNode node = it.next();
                YamlMapping mapping = node.asMapping();
                //添加映射缓存
                String platform = mapping.string(ConfigDanMuAutoSendTaskField.VIDEO_PLATFORM.getFieldString());
                String uid = mapping.string(ConfigDanMuAutoSendTaskField.LISTEN_UP_UID.getFieldString());
                //当填入的uid小于等于0时，跳过
                if (Convert.toInt(uid, 0) <= 0) {
                    continue;
                }
                String saveName = mapping.string(ConfigDanMuAutoSendTaskField.LINK_DANMU_SAVE_NAME.getFieldString());
                videoUidAndSaveFileNameMap.put(platform + link + uid, saveName);
                String tagMatch = mapping.string(ConfigDanMuAutoSendTaskField.TAG_MATCH.getFieldString());
                String titleMatch = mapping.string(ConfigDanMuAutoSendTaskField.TITLE_MATCH.getFieldString());
                //根据平台创建对应任务
                VideoUpdateTask videoUpdateTask;
                switch (v) {
                    case BILIBILI:
                    default:
                        videoUpdateTask = new BiliVideoUpdateTask(uid, tagMatch, titleMatch, regular, timeFormat,
                                //从发送账户中获取cookies
                                CustomConfigUtil.getSenderCookie());
                }
                listenIdTaskMap.put(uid,videoUpdateTask);
            }
            platformListenMap.put(v.getName(), listenIdTaskMap);
        });
        //添加动态监听
        Map<String, VideoUpdateTask> listenIdTaskMap = platformListenMap.get(VideoPlatform.BILIBILI.getName());
        logger.info("正在添加账户监听...");
        if (listenIdTaskMap != null) {
            logger.info("账户监听列表:{}", listenIdTaskMap.keySet());
            listenIdTaskMap.values().forEach(biliVideoUpdateListenService::startVideoUpdateListen);
        }
        logger.info("弹幕发送控制器初始化完成，等待定时任务执行...");
    }

    /**
     * 从数据库遍历可用任务的定时任务
     * @return  创建的任务
     */
    public Runnable createQueuePushTask() {
        return () -> {
            if (allowAutoPushQueue && danmuSenderTaskModelQueue.isEmpty()) {
                MainDatabaseService mainDatabaseService = MainDatabaseService.getInstance();
                //查询未完成任务，优先填入创建时间最早的
                List<DanmuSenderTaskModel> danmuSenderTaskModelList = mainDatabaseService.getListByFlag(false, false, false, queueNum)
                        .stream()
                        .sorted(Comparator.comparing(DanmuSenderTaskModel::getCreateTime))
                        .collect(Collectors.toList());
                danmuSenderTaskModelQueue.addAll(danmuSenderTaskModelList);
                if (!danmuSenderTaskModelList.isEmpty()) {
                    logger.info("更新队列成功，当前等待发送的队列：{}",danmuSenderTaskModelQueue.stream().map(DanmuSenderTaskModel::getVideoId).collect(Collectors.joining(",")));
                }
            }
        };
    }

    public Runnable createStartSendTask() {
        return () -> {
            if (!danmuSenderTaskModelQueue.isEmpty()) {
                DanmuSenderTaskModel taskModel = danmuSenderTaskModelQueue.poll();
                String videoId = taskModel.getVideoId();
                BiliProcessedVideoData biliProcessedVideoData;
                try {
                    biliProcessedVideoData = BiliVideoUtil.matchVideo(videoId
                            , regular,
                            timeFormat,
                            null, null);
                } catch (ServiceException e) {
                    logger.error(e.getMessage(),e.getOriginalException());
                    return;
                }
                String saveName = videoUidAndSaveFileNameMap.get(VideoPlatform.BILIBILI.getName() + link + taskModel.getVideoCreatorUid());
                if (saveName == null || saveName.trim().length() == 0) {
                    logger.error("尝试创建{}的弹幕发送任务失败!没有在配置文件找到正确的弹幕保存名",taskModel.getVideoId());
                    return;
                }
                try {
                    danMuAutoSendService.startSendTask(biliProcessedVideoData,saveName);
                }
                //默认重复添加会拦截，此异常可忽略
                catch (ServiceException e) {
                    logger.trace("尝试创建{}的弹幕发送任务失败!",taskModel.getVideoId(),e);
                }
            }
        };
    }

    public void stopTask() {
        danMuAutoSendService.stopSendTask();
    }


//
//    public void addTaskByNormalConfigFile() {
////        danMuAutoSendService.startSendTask();
//
//
//        //多平台实现，当前过于复杂
////        Map<String, List<? extends DanMuSenderAccountData>> platformAccountMap = new HashMap<>(VideoPlatform.values().length);
////        for (VideoPlatform v :
////                VideoPlatform.values()) {
////            List<YamlMapping> mappingList = accountList.asStream()
////                    .map(YamlNode::asMapping)
////                    .filter(m -> v.getName().equals(m.string(ConfigDanMuAutoSendAccountField.VIDEO_PLATFORM.getFieldString()))).collect(Collectors.toList());
////            switch (v) {
////                case BILIBILI:default:
////                    List<BiliDanMuSenderAccountData> accountDataList = mappingList.stream()
////                            .map(m -> {
////                                BiliDanMuSenderAccountData biliDanMuSenderAccountData = new BiliDanMuSenderAccountData();
////                                biliDanMuSenderAccountData.setNickName(m.string(ConfigDanMuAutoSendAccountField.NICK_NAME.getFieldString()));
////                                biliDanMuSenderAccountData.setUserName(m.string(ConfigDanMuAutoSendAccountField.USER_NAME.getFieldString()));
////                                biliDanMuSenderAccountData.setPassword(m.string(ConfigDanMuAutoSendAccountField.PASSWORD.getFieldString()));
////                                biliDanMuSenderAccountData.setPassword(m.string(ConfigDanMuAutoSendAccountField.PASSWORD.getFieldString()));
////                                biliDanMuSenderAccountData.setCookies(m.string(ConfigDanMuAutoSendAccountField.COOKIES.getFieldString()));
////                                return biliDanMuSenderAccountData;
////                            })
////                            .collect(Collectors.toList());
////                    platformAccountMap.put(v.getName(), accountDataList);
////                    break;
////            }
////        }
//        //装配服务类
////        for (VideoPlatform v :
////                VideoPlatform.values()) {
////            switch (v) {
////                case BILIBILI:default:
////                    List<BiliDanMuSenderAccountData> biliDanMuSenderAccountDataList = (List<BiliDanMuSenderAccountData>) platformAccountMap.get(v.getName());
////                    BaseDanMuAutoSendService<BiliDanMuSenderAccountData, BiliProcessedVideoData> danMuAutoSendService = BiliDanMuAutoSendServiceImpl.getInstance(biliDanMuSenderAccountDataList);
////                    break;
////            }
////        }
//    }
}
