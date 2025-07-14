package com.github.cuteluobo.livedanmuarchive.async;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendAccountField;
import com.github.cuteluobo.livedanmuarchive.enums.danmu.send.VideoPlatform;
import com.github.cuteluobo.livedanmuarchive.exception.AuthInvalidException;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuTaskPlanModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.BaseUserInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedPartVideoData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import com.github.cuteluobo.livedanmuarchive.service.database.MainDatabaseService;
import com.github.cuteluobo.livedanmuarchive.utils.BiliDanMuUtil;
import com.github.cuteluobo.livedanmuarchive.utils.BiliLoginUtil;
import com.github.cuteluobo.livedanmuarchive.utils.CustomConfigUtil;
import com.github.cuteluobo.livedanmuarchive.utils.reader.BatchSqliteDanMuReader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 新的B站弹幕发送实现类
 *
 * @author: CuteLuoBo
 * @date: 2025/7/8  10:59
 * @version: 1.0.0
 */
public class BiliDanMuSenderNew extends AbstractDanMuSender {
    public BiliDanMuSenderNew(BatchSqliteDanMuReader sqliteDanMuReader) {
        super(sqliteDanMuReader);
        initSenderStatus();
//        int size = Math.max(baseUserInfoList.size(),1);
//        executor = new ThreadPoolExecutor(size, size,
//                3, TimeUnit.SECONDS,
//                new ArrayBlockingQueue<>(3), new NamedThreadFactory("弹幕发送线程", false));
    }


    /**
     * 轮次缓存map
     * key:轮次
     * value:当前轮次的弹幕数据(仅有ID和时间)
     */
    private Map<Long,List<DanMuDataModel>> roundTempMap = new HashMap<>();

    private long startTimeTemp = 0;
    private long endTimeTemp = 0;

    /**
     * 弹幕发送用线程池
     */
//    private ThreadPoolExecutor executor;

    private List<BaseUserInfo> baseUserInfoList;

    private static final String omissionMark = "...";


    /**
     * 初始化弹幕发送账户状态
     */
    public void initSenderStatus(){
        YamlMapping accountMainConfig = CustomConfigUtil.INSTANCE.getConfigMapping().yamlMapping(ConfigDanMuAutoSendAccountField.MAIN_FIELD.getFieldString());
        YamlSequence accountList = accountMainConfig.yamlSequence(ConfigDanMuAutoSendAccountField.ACCOUNT_LIST.getFieldString());
        baseUserInfoList = new ArrayList<>(accountList.size());
        int num = 0;
        for (YamlNode node : accountList) {
            num++;
            YamlMapping mapping = node.asMapping();
            String tempCookie = mapping.string(ConfigDanMuAutoSendAccountField.COOKIES.getFieldString());
            try {
                BaseUserInfo baseUserInfo;
                if (!StrUtil.isEmpty(tempCookie)) {
                    baseUserInfo = BiliLoginUtil.getUserBaseInfoByCk(tempCookie);
                    //有登录状态时，填充并读取下一个
                    if (baseUserInfo.isLogin()) {
                        baseUserInfoList.add(baseUserInfo);
                        logger.info("获取第{}位账户的Cookie登录状态成功", num);
                        continue;
                    }
                }
                //CK为空或CK登录失败时，尝试AppKey登录
                baseUserInfo = BiliLoginUtil.getUserBaseInfoByAppKey(
                        mapping.string(ConfigDanMuAutoSendAccountField.ACCESS_KEY.getFieldString()),
                        mapping.string(ConfigDanMuAutoSendAccountField.APP_KEY.getFieldString()),
                        mapping.string(ConfigDanMuAutoSendAccountField.APP_SEC.getFieldString()));
                //有登录状态时，填充并读取下一个
                if (baseUserInfo.isLogin()) {
                    baseUserInfoList.add(baseUserInfo);
                    logger.info("获取第{}位账户的App_Key登录状态成功", num);
                    continue;
                }
                logger.warn("获取第{}位账户的账户登录状态失败，跳过", num);
            } catch (URISyntaxException | RuntimeException exception) {
                logger.warn("获取第{}位账户的Cookie登录状态失败，跳过", num, exception);
            }
        }
    }

    /**
     * 验证运行条件
     *
     * @param processedVideoData
     * @return
     */
    @Override
    public boolean validatePreconditions(BiliProcessedVideoData processedVideoData) {
        if (baseUserInfoList.isEmpty()) {
            logger.error("无可用的的弹幕发送账户，跳过任务");
            return false;
        }
        return super.validatePreconditions(processedVideoData);
    }

    /**
     * 执行弹幕发送
     *
     * @param queue
     */
    @Override
    public void startDanMuSender(BlockingQueue<RetryTask<DanMuData>> queue, BiliProcessedPartVideoData partVideoData) {
        List<CompletableFuture<?>> tasks = new ArrayList<>();
        //使用可用的账户列表，创建弹幕发送任务
        for (BaseUserInfo info : baseUserInfoList) {
            //有正常的登录状态时，才添加线程
            if (info.isLogin()) {
                CompletableFuture<?> task = createDanMuSenderTask(queue, info, partVideoData);
                tasks.add(task);
            }
//            executor.execute(() -> {
//                try {
//                    task.join();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            });
        }
        //同步等待所有子任务完成（发送完成队列内的弹幕）
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
    }

    /**
     * 单个账户的弹幕发送任务，从队列中获取消息并发送
     * @param queue
     * @param baseUserInfo
     * @return
     */
    private CompletableFuture<?> createDanMuSenderTask(BlockingQueue<RetryTask<DanMuData>> queue,BaseUserInfo baseUserInfo,BiliProcessedPartVideoData partVideoData) {
        return CompletableFuture.runAsync(() -> {
            //初始化变量
            boolean allowColor = false, allowExpertDanMu = false, allowTopDanMu = false, allowBottomDanMu = false, allowMoreText = false;
            //检查账户等级
            if (baseUserInfo.getLevel() > 1) {
                allowColor = true;
                allowExpertDanMu = true;
                allowMoreText = true;
                if (baseUserInfo.getLevel() > 2) {
                    allowTopDanMu = true;
                    allowBottomDanMu = true;
                }
            }
            //检查账户允许的最长弹幕
            int maxContentLength = 100;
            if (!allowMoreText) {
                logger.warn("当前{}账户，等级过低，长度超过20的弹幕消息将被省略", baseUserInfo.getNickName());
                maxContentLength = 20;
            }
            //默认发送延时
            long baseDelayTime = danMuSendNormalDelay;
            long stepDelayTime = danMuSendFastFailDelay;
            long maxDelayTime = danMuSendNormalMaxDelay;
            //额外延迟时间（用于失败场景）
            long extraDelayTime = RandomUtil.randomLong(danMuSendRandomMinDelay, danMuSendRandomMaxDelay);
            long totalDelayTime = baseDelayTime + extraDelayTime;
            //循环获取队列消息并发送
            while (!queue.isEmpty() && isContinue && baseUserInfo.isLogin()) {
                //延时等待
                try {
                    TimeUnit.MILLISECONDS.sleep(totalDelayTime);
                } catch (InterruptedException e) {
                    logger.error("{}账户，延时发送弹幕线程异常中断", baseUserInfo.getNickName(), e);
                }
                RetryTask<DanMuData> danMuDataRetryTask = queue.poll();
                if (danMuDataRetryTask == null) {
                    continue;
                }
                DanMuData danMuData = danMuDataRetryTask.getTask();
                //检查超长内容
                contentLengthResize(maxContentLength, danMuData);

                //发送弹幕并获取发送结果
                boolean sendResult;
                try {
                    sendResult = sendDanMu(baseUserInfo, danMuDataRetryTask, allowColor, partVideoData.getCid(), partVideoData.getBvId(), partVideoData.getVideoStartMillTime());
                } catch (AuthInvalidException authInvalidException) {
                    logger.error("{}账户登录状态失效，跳过该账户", baseUserInfo.getNickName(), authInvalidException);
                    baseUserInfo.setLogin(false);
                    Thread.currentThread().interrupt();
                    continue;
                }
                //发送失败时，增加延时
                long tempDelayTime;
                if (!sendResult) {
                    //当前时间+随机时间+递增时间
                    tempDelayTime = totalDelayTime + RandomUtil.randomLong(danMuSendRandomMinDelay, danMuSendRandomMaxDelay) + stepDelayTime ;
                    totalDelayTime = Math.min(tempDelayTime, maxDelayTime);
                    logger.info("弹幕发送失败，增加延时,当前{} ms",totalDelayTime);
                    //当失败时，检查是否还有重试次数，有次数时重新加入队列
                    if (danMuDataRetryTask.decreaseRetryCount()) {
                        queue.add(danMuDataRetryTask);
                        logger.debug("重新加入队列，当前重试次数：{}",danMuDataRetryTask.getRetryCount());
                    } else {
                        logger.debug("弹幕发送失败，重试次数已用完，跳过该条弹幕");
                    }
                } else {
                    tempDelayTime = totalDelayTime - stepDelayTime / 2;
                    //发送成功时，缩减延时
                    totalDelayTime = Math.max(tempDelayTime,baseDelayTime);
                    logger.info("弹幕发送成功，缩减延时，当前{} ms",totalDelayTime);
                }

            }
        });
    }

    /**
     * 弹幕内容长度处理
     * @param maxContentLength 最大长度
     * @param danMuData 弹幕数据
     * @return 处理后的弹幕数据
     */
    private void contentLengthResize(int maxContentLength, DanMuData danMuData){
        if (danMuData.getContent().length() > maxContentLength) {
            danMuData.setContent(danMuData.getContent().substring(0, maxContentLength - omissionMark.length()) + omissionMark);
        }
    }

    private boolean sendDanMu(BaseUserInfo baseUserInfo,RetryTask<DanMuData> retryTask,boolean allowColor,long cid,String bvId,long videoStartTime){
        DanMuData danMuData = retryTask.getTask();
        //发送请求
        String bodyString;
        try {
            bodyString = BiliDanMuUtil.sendDanMuByNetty(
                    cid,
                    danMuData.getContent(),
                    bvId,
                    0,
                    danMuData.getTimestamp() - videoStartTime,
                    allowColor?danMuData.getDanMuFormatData().getFontColor():16777215,
                    allowColor?(float) danMuData.getDanMuFormatData().getFontSize():25,
                    0,
                    1,baseUserInfo.getImgKey(),baseUserInfo.getSubKey(), baseUserInfo.getCookie(), baseUserInfo.getAccessKey());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.debug("请求异常中断", e);
            return false;
        }
        //解析发送结果
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(bodyString);
        } catch (JsonProcessingException e) {
            logger.error("解析返回消息失败,消息原文：{}",bodyString,e);
            return false;
        }
        if (jsonNode != null) {
            JsonNode codeNode = jsonNode.get("code");
            //判断发送是否成功
            if (codeNode != null) {
                int code = codeNode.asInt();
                senderCount.getTotal().incrementAndGet();
                if (code == 0) {
                    senderCount.getSuccess().incrementAndGet();
                    logger.info("{}账户，发送弹幕成功({}/{}/{})，({}:)\"{}\"", baseUserInfo.getNickName(),
                            senderCount.getSuccess().get(),senderCount.getFail().get(), senderCount.getTotal().get(),
                            danMuData.getUserIfo() == null ? "?" : danMuData.getUserIfo().getNickName(), danMuData.getContent());
                    return true;
                } else {
                    senderCount.getFail().incrementAndGet();
                    JsonNode messageNode = jsonNode.get("message");
                    logger.error("{}账户，发送弹幕({}:)\"{}\"出现问题：code:{},message:{}", baseUserInfo.getNickName(),danMuData.getUserIfo()==null?"?":danMuData.getUserIfo().getNickName(),danMuData.getContent(), code, messageNode == null ? "(api未返回消息)" : messageNode.asText());
                    //弹幕发送过快
                    if (code == 36703) {
                        //发送过快时，添加重试次数
                        retryTask.setRetryCount(retryTask.getRetryCount() + 1);
                    }
                    if (code == 36715) {
                        //TODO 待增加单日发送超限处理
                    }
                    if (code < 0) {
                        throw new AuthInvalidException(String.format("%s账户登录状态失效", baseUserInfo.getNickName()));
                    }
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 保存弹幕任务执行结果
     *
     * @param taskPlan 旧的任务
     */
    @Override
    public void saveTaskPlan(DanMuTaskPlanModel taskPlan) {
        MainDatabaseService.getInstance().updateTaskPlan(taskPlan);
    }

    /**
     * 根据任务获取弹幕数据
     * @param taskPlan 任务
     * @param partVideoData 视频分P信息
     * @return 弹幕数据
     */
    @Override
    public List<DanMuData> getSendDanMuList(BiliProcessedPartVideoData partVideoData, DanMuTaskPlanModel taskPlan) {
        DanMuDataModelSelector danMuDataModelSelector = new DanMuDataModelSelector(
                partVideoData.getVideoStartMillTime(),
                partVideoData.getVideoEndMillTime());
        //当起始时间变化时，重新从数据库中获取数据
        if (!Objects.equals(startTimeTemp, partVideoData.getVideoStartMillTime()) || !Objects.equals(endTimeTemp, partVideoData.getVideoEndMillTime())) {
            List<DanMuDataModel> danMuDataModelList = sqliteDanMuReader.listTimeDataByTime(danMuDataModelSelector);
            roundTempMap.clear();
            splitDanMuDataModelTimeList(partVideoData.getVideoStartMillTime(),danMuDataModelList);
            startTimeTemp = partVideoData.getVideoStartMillTime();
            endTimeTemp = partVideoData.getVideoEndMillTime();
        }
        List<DanMuDataModel> roundModelList = roundTempMap.getOrDefault(Long.valueOf(taskPlan.getPageCurrent()), new ArrayList<>());
        //为空时返回空列表
        if (roundModelList.isEmpty()) {
            return new ArrayList<>();
        }
        //读取数据转为idMap
        Map<Long,Long> idMap = roundModelList.stream().collect(Collectors.toMap(DanMuDataModel::getId, DanMuDataModel::getCreateTime));
        //读取数据
        List<DanMuData> danMuDataList = sqliteDanMuReader.listModelByIdAndCreateTimeMap(danMuDataModelSelector, idMap);
        return danMuDataList;
    }

    /**
     * 分割弹幕列表数据，并存到不同轮数的缓存map中
     * @param startTime 视频开始时间
     * @param danMuDataModelList 弹幕数据列表
     */
    private void splitDanMuDataModelTimeList(long startTime,List<DanMuDataModel> danMuDataModelList) {
        if (danMuDataModelList.isEmpty()) {
            return;
        }
        //时间窗口划分map 例：(1=0~20s弹幕数据,2=20~40s弹幕数据)
        Map<Long, List<DanMuDataModel>> tickTemp = new HashMap<>();
        //根据划分的时间窗口进行切割
        danMuDataModelList.forEach(danMuDataModel -> {
            //获取时间差（对开始时间）
            long diffTime = danMuDataModel.getCreateTime() - startTime;
            //根据间隔划分时间窗口
            long tick = diffTime / danMuSplitTime
;
            //获取并添加到时间窗口对应的列表中
            List<DanMuDataModel> tempList = tickTemp.getOrDefault(tick, new ArrayList<>());
            tempList.add(danMuDataModel);
            tickTemp.put(tick, tempList);
        });
        //将时间窗口划分的弹幕的前roundLimit数据进行填充
        tickTemp.forEach((key, value) -> {
            //获取当前轮次的弹幕数据
            if (value.isEmpty()) {
                return;
            }
            //当允许高能弹幕时间，并弹幕数量大于阈值时，将当前时间内的弹幕数据一次性添加到轮次列表中
            if (danMuAllowPeakTime && value.size() > danMuPeakTimeThreshold) {
                //添加高能弹幕限制内的数据到第0轮弹幕列表
                long round = 0;
                List<DanMuDataModel> roundList = roundTempMap.getOrDefault(round, new ArrayList<>());
                roundList.addAll(value.stream().limit(danMuPeakTimeMax).collect(Collectors.toList()));
                roundTempMap.put(round, roundList);
            }
            //否则分轮次储存（获取前roundLimit的数据存入，其余丢弃）
            else{
                int start = Math.min(danMuStartRound, value.size() - 1);
                int max = Math.min(value.size(), danMuEndRound + 1);
                for (long round = start; round < max; round++) {
                    //获取当前轮次的弹幕数据
                    List<DanMuDataModel> roundList = roundTempMap.getOrDefault(round, new ArrayList<>());
                    roundList.add(value.get((int) round));
                    roundTempMap.put(round, roundList);
                }
            }
        });
    }

    /**
     * 获取历史任务
     *
     * @param partVideoData 视频信息
     * @return 查询结果，为Null时说明没有历史任务
     */
    @Override
    public DanMuTaskPlanModel getHistoryTask(BiliProcessedPartVideoData partVideoData) {
        return MainDatabaseService.getInstance().getOneTaskPlanByVideoId(String.valueOf(partVideoData.getCid()));
    }

    /**
     * 根据当前数据创建新的任务进度对象
     *
     * @param partVideoData 视频分P信息
     * @return 新的任务进度对象
     */
    @Override
    public DanMuTaskPlanModel createDanmuAccountTaskModel(BiliProcessedPartVideoData partVideoData) {
        DanMuTaskPlanModel danMuTaskPlanModel = new DanMuTaskPlanModel();
        danMuTaskPlanModel.setPlatform(VideoPlatform.BILIBILI.getName());
        danMuTaskPlanModel.setVideoId(String.valueOf(partVideoData.getCid()));
        danMuTaskPlanModel.setVideoCreatorUid("");
        danMuTaskPlanModel.setVideoCreatedTime(0L);
        danMuTaskPlanModel.setPageCurrent(0);
        danMuTaskPlanModel.setSlicedTime(danMuSplitTime);
        long time = System.currentTimeMillis();
        danMuTaskPlanModel.setCreateTime(time);
        danMuTaskPlanModel.setUpdateTime(time);
        danMuTaskPlanModel.setTaskFail(false);
        danMuTaskPlanModel.setSkip(false);
        danMuTaskPlanModel.setFinishTime(0L);
        MainDatabaseService.getInstance().addTaskPlan(danMuTaskPlanModel);
        return danMuTaskPlanModel;
    }
}
