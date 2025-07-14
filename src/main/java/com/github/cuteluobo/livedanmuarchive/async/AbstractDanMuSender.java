package com.github.cuteluobo.livedanmuarchive.async;

import cn.hutool.core.convert.Convert;
import com.amihaiemil.eoyaml.YamlMapping;
import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendAccountField;
import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendTaskField;
import com.github.cuteluobo.livedanmuarchive.model.DanMuTaskPlanModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedPartVideoData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.SenderCount;
import com.github.cuteluobo.livedanmuarchive.utils.CustomConfigUtil;
import com.github.cuteluobo.livedanmuarchive.utils.reader.BatchSqliteDanMuReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 弹幕发送者抽象类
 *
 * @author: CuteLuoBo
 * @date: 2025/7/8  9:40
 * @version: 1.0.0
 */
public abstract class AbstractDanMuSender {
    Logger logger = LoggerFactory.getLogger(AbstractDanMuSender.class);
    /**
     * 批量sqlite数据库弹幕读取类
     */
    protected BatchSqliteDanMuReader sqliteDanMuReader;

    public AbstractDanMuSender(BatchSqliteDanMuReader sqliteDanMuReader) {
        this.sqliteDanMuReader = sqliteDanMuReader;
        reloadConfig();
    }

    /**
     * 弹幕起始轮次
     */
    protected int danMuStartRound = 0;
    /**
     * 弹幕结束轮次
     */
    protected int danMuEndRound = 4;
    /**
     * 每批弹幕截取间隔时间(ms)
     */
    protected long danMuSplitTime = 5000;
    /**
     * 是否运行高能时间
     */
    protected boolean danMuAllowPeakTime = true;
    /**
     * 时间段内高能弹幕时间触发阈值
     */
    protected int danMuPeakTimeThreshold = 15;
    /**
     * 高能弹幕时间包含的弹幕上限
     */
    protected int danMuPeakTimeMax = 30;

    protected long danMuSendNormalDelay = 6000;
    protected long danMuSendNormalMaxDelay = 45000;
    protected long danMuSendRandomMaxDelay = 5000;
    protected long danMuSendRandomMinDelay = 0;
    protected long danMuSendFastFailDelay = 10000;

    /**
     * 重载配置文件
     */
    public void reloadConfig() {
        //读取配置文件
        CustomConfigUtil customConfigUtil = CustomConfigUtil.INSTANCE;
        YamlMapping allConfig = customConfigUtil.getConfigMapping();
        YamlMapping taskMainConfig = allConfig.yamlMapping(ConfigDanMuAutoSendTaskField.MAIN_FIELD.getFieldString());
        //起始轮次
        int danMuStartRoundTimeTemp = taskMainConfig.integer(ConfigDanMuAutoSendTaskField.DANMU_START_ROUND.getFieldString());
        if (danMuStartRoundTimeTemp == -1) {
            danMuStartRound = Convert.toInt(ConfigDanMuAutoSendTaskField.DANMU_START_ROUND.getNormalValue());
        }else {
            danMuStartRound = danMuStartRoundTimeTemp;
        }
        //结束轮次
        int danMuEndRoundTimeTemp = taskMainConfig.integer(ConfigDanMuAutoSendTaskField.DANMU_END_ROUND.getFieldString());
        if (danMuEndRoundTimeTemp == -1) {
            danMuEndRound = Convert.toInt(ConfigDanMuAutoSendTaskField.DANMU_END_ROUND.getNormalValue());
        }else {
            danMuEndRound = danMuEndRoundTimeTemp;
        }
        //分片时间
        int danMuSplitTimeTemp = taskMainConfig.integer(ConfigDanMuAutoSendTaskField.DANMU_SPLIT_TIME.getFieldString());
        if (danMuSplitTimeTemp == -1) {
            danMuSplitTime = Convert.toLong(ConfigDanMuAutoSendTaskField.DANMU_SPLIT_TIME.getNormalValue());
        }else {
            danMuSplitTime = danMuSplitTimeTemp;
        }
        //高能时间
        danMuAllowPeakTime = Convert.toBool(taskMainConfig.string(ConfigDanMuAutoSendTaskField.DANMU_ALLOW_PEAK_TIME.getFieldString())
                ,Convert.toBool(ConfigDanMuAutoSendTaskField.DANMU_ALLOW_PEAK_TIME.getNormalValue()));
        //高能时间触发阈值
        int danMuPeakTimeThresholdTemp = taskMainConfig.integer(ConfigDanMuAutoSendTaskField.DANMU_PEAK_TIME_THRESHOLD.getFieldString());
        if (danMuPeakTimeThresholdTemp == -1) {
            danMuPeakTimeThreshold = Convert.toInt(ConfigDanMuAutoSendTaskField.DANMU_PEAK_TIME_THRESHOLD.getNormalValue());
        }else {
            danMuPeakTimeThreshold = danMuPeakTimeThresholdTemp;
        }
        //高能时间最大弹幕数
        int danMuPeakTimeMaxTemp = taskMainConfig.integer(ConfigDanMuAutoSendTaskField.DANMU_PEAK_TIME_MAX.getFieldString());
        if (danMuPeakTimeMaxTemp == -1) {
            danMuPeakTimeMax = Convert.toInt(ConfigDanMuAutoSendTaskField.DANMU_PEAK_TIME_MAX.getNormalValue());
        }else {
            danMuPeakTimeMax = danMuPeakTimeMaxTemp;
        }
        YamlMapping autoSendMapping = allConfig.yamlMapping(ConfigDanMuAutoSendAccountField.MAIN_FIELD.getFieldString());
        //发送间隔
        danMuSendNormalDelay = Convert.toInt(autoSendMapping.string(ConfigDanMuAutoSendAccountField.SEND_NORMAL_DELAY.getFieldString())
                ,Convert.toInt(ConfigDanMuAutoSendAccountField.SEND_NORMAL_DELAY.getNormalValue()));
        danMuSendNormalMaxDelay = Convert.toInt(autoSendMapping.string(ConfigDanMuAutoSendAccountField.SEND_NORMAL_MAX_DELAY.getFieldString())
               ,Convert.toInt(ConfigDanMuAutoSendAccountField.SEND_NORMAL_MAX_DELAY.getNormalValue()));
        danMuSendRandomMaxDelay = Convert.toInt(autoSendMapping.string(ConfigDanMuAutoSendAccountField.SEND_RANDOM_MAX_DELAY.getFieldString())
               ,Convert.toInt(ConfigDanMuAutoSendAccountField.SEND_RANDOM_MAX_DELAY.getNormalValue()));
        danMuSendRandomMinDelay = Convert.toInt(autoSendMapping.string(ConfigDanMuAutoSendAccountField.SEND_RANDOM_MIN_DELAY.getFieldString())
              ,Convert.toInt(ConfigDanMuAutoSendAccountField.SEND_RANDOM_MIN_DELAY.getNormalValue()));
        danMuSendFastFailDelay = Convert.toInt(autoSendMapping.string(ConfigDanMuAutoSendAccountField.SEND_FAST_FAIL_DELAY.getFieldString())
             ,Convert.toInt(ConfigDanMuAutoSendAccountField.SEND_FAST_FAIL_DELAY.getNormalValue()));
    }

    /**
     * 发送弹幕的统计数据（成功/失败，总计）
     */
    protected SenderCount senderCount = new SenderCount();
    /**
     * 弹幕储存的并发队列
     */
    protected BlockingQueue<RetryTask<DanMuData>> queue = new LinkedBlockingQueue<>();
    protected int retryCount = 1;
    protected boolean isContinue = true;
    /**
     * 启动发送者任务
     */
    public void runSender(BiliProcessedVideoData processedVideoData){
        //检验任务前置条件
        if (validatePreconditions(processedVideoData)){
            isContinue = true;
            DanMuTaskPlanModel taskPlan = null;
            //获取分P数据
            for (BiliProcessedPartVideoData partVideoData : processedVideoData.getPartVideoDataList()) {
                logger.info("稿件{}的单P {}:{} 尝试读取并发送弹幕", processedVideoData.getBvId(), partVideoData.getCid(), partVideoData.getPartName());
                //获取历史任务
                taskPlan = getHistoryTask(partVideoData);
                //检查分P弹幕是否已经完成
                if (taskPlan!=null && taskPlan.getFinishTime() != null && taskPlan.getFinishTime() > 0) {
                    logger.info("稿件{}的单P {}:{} 弹幕发送已有完成记录，跳过", processedVideoData.getBvId(), partVideoData.getCid(), partVideoData.getPartName());
                    continue;
                }
                //没有历史任务时，根据当前稿件数据创建
                if (taskPlan == null) {
                    logger.info("稿件{}的单P {}:{} 未找到历史任务，启用新的任务", processedVideoData.getBvId(), partVideoData.getCid(), partVideoData.getPartName());
                    taskPlan = createDanmuAccountTaskModel(partVideoData);
                    //从指定轮次开始
                    taskPlan.setPageCurrent(Math.max(taskPlan.getPageCurrent(), danMuStartRound));
                }else{
                    //有记录时，起始记录+1（避免重启后重复发送）
                    taskPlan.setPageCurrent(Math.max(taskPlan.getPageCurrent(), danMuStartRound) + 1);
                    saveTaskPlan(taskPlan);
                    logger.info("稿件{}的单P {}:{} 找到历史任务，进行第{} / {}轮", processedVideoData.getBvId(), partVideoData.getCid(), partVideoData.getPartName(), taskPlan.getPageCurrent(), danMuEndRound);
                }
                int round = taskPlan.getPageCurrent();
                //没有收到终止信号时，循环获取稿件对应的弹幕，并执行发送
                while (isContinue) {
                    //获取当前分P数据
                    List<DanMuData> danMuDataList = getSendDanMuList(partVideoData, taskPlan);
                    //弹幕数据为空时，跳过，进行下一P视频的发送
                    if (danMuDataList.isEmpty() || round > danMuEndRound
) {
                        if (round > danMuEndRound
) {
                            logger.info("稿件{}的单P {}:{} 弹幕发送完成(发送轮次超过阈值:{})，跳转到下P", processedVideoData.getBvId(), partVideoData.getCid(), partVideoData.getPartName(), danMuEndRound
);
                        }else{
                            logger.info("稿件{}的单P {}:{} 发送完成，没有更多弹幕，跳转到下P", processedVideoData.getBvId(), partVideoData.getCid(), partVideoData.getPartName());
                        }
                        //结束保存任务信息(单P已完成)
                        taskPlan.setFinishTime(System.currentTimeMillis());
                        saveTaskPlan(taskPlan);
                        break;
                    }
                    logger.info("当前为第{}轮，读取弹幕数量：{}", round, danMuDataList.size());
                    //对获取的弹幕数据进行处理
                    danMuDataList = preHandler(danMuDataList);
                    //填充队列
                    pushQueue(danMuDataList, retryCount);
                    //开始弹幕发送
                    startDanMuSender(queue,partVideoData);
                    //下次获取时从下一页开启
                    taskPlan.setPageCurrent(++round);
                    //每轮结束保存任务信息
                    saveTaskPlan(taskPlan);
                }
                if (!isContinue) {
                    logger.info("收到停止信号，{}稿件的单P{}:{} 弹幕发送任务终止，当前{}轮，弹幕发送执行结果：{}",processedVideoData.getBvId(),partVideoData.getCid(),partVideoData.getPartName(),round, senderCount);
                    //停止任务或任务被终止时，将当前进行任务保存到数据库中
                    saveTaskPlan(taskPlan);
                    //收到停止信号时，终止方法
                    return;
                }
            }
            //稿件中的所有弹幕发送完成时，进行
            if (taskPlan != null) {
                taskPlan.setFinishTime(System.currentTimeMillis());
                saveTaskPlan(taskPlan);
            }
        }
    }

    /**
     * 对弹幕数据进行处理
     * 例如：弹幕数据去重、敏感消息过滤
     * @param danMuDataList 弹幕数据
     * @return 处理后的数据
     */
    public List<DanMuData> preHandler(List<DanMuData> danMuDataList){
        return danMuDataList;
    };

    /**
     * 执行弹幕发送
     * @param queue 并发弹幕队列
     */
    public abstract void startDanMuSender(BlockingQueue<RetryTask<DanMuData>> queue,BiliProcessedPartVideoData partVideoData);


    /**
     * 保存弹幕任务执行结果
     * @param taskPlan 旧的任务
     */
    public abstract void saveTaskPlan(DanMuTaskPlanModel taskPlan);

    /**
     * 填充队列
     * @param danMuDataList 弹幕数据
     * @param retryCount 重试次数
     */
    public void pushQueue(List<DanMuData> danMuDataList,int retryCount){
        for (DanMuData danMuData : danMuDataList) {
            if (danMuData == null) {
                continue;
            }
            queue.add(new RetryTask<>(danMuData,retryCount));
        }
    };

    /**
     * 失败后尝试添加回队列
     * @param task 任务
     */
    public void failRePushQueue(RetryTask<DanMuData> task){
        if (task.decreaseRetryCount()) {
            queue.add(task);
        }else {
            logger.debug("{}弹幕超过重试次数，丢弃",task.getTask().getContent());
        }
    }

    /**
     * 根据任务获取弹幕数据
     * @param taskPlan 任务
     * @param partVideoData 视频分P信息
     * @return 弹幕数据
     */
    public abstract List<DanMuData> getSendDanMuList(BiliProcessedPartVideoData partVideoData,DanMuTaskPlanModel taskPlan);

    /**
     * 获取历史任务
     * @param partVideoData 视频分P信息
     * @return 查询结果，为Null时说明没有历史任务
     */
    public abstract DanMuTaskPlanModel getHistoryTask(BiliProcessedPartVideoData partVideoData);

    /**
     * 根据当前数据创建新的任务进度对象
     * @param partVideoData 视频分P信息
     * @return 新的任务进度对象
     */
    public abstract DanMuTaskPlanModel createDanmuAccountTaskModel(BiliProcessedPartVideoData partVideoData);

    public void setStop(){
        isContinue = false;
    }
    /**
     * 验证运行条件
     * @return
     */
    public boolean validatePreconditions(BiliProcessedVideoData processedVideoData){
        if (sqliteDanMuReader == null) {
            logger.error("未传入可获取的弹幕获取对象，跳过任务");
            return false;
        }
        if (processedVideoData.getPageIndexList() == null) {
            logger.warn("传入的视频信息内没有分P缓存数据");
            return false;
        }
        return true;
    }

    public BatchSqliteDanMuReader getSqliteDanMuReader() {
        return sqliteDanMuReader;
    }

    public void setSqliteDanMuReader(BatchSqliteDanMuReader sqliteDanMuReader) {
        this.sqliteDanMuReader = sqliteDanMuReader;
    }

    public SenderCount getSenderCount() {
        return senderCount;
    }

    public void setSenderCount(SenderCount senderCount) {
        this.senderCount = senderCount;
    }

    public BlockingQueue<RetryTask<DanMuData>> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<RetryTask<DanMuData>> queue) {
        this.queue = queue;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isContinue() {
        return isContinue;
    }

    public void setContinue(boolean aContinue) {
        this.isContinue = aContinue;
    }
}
