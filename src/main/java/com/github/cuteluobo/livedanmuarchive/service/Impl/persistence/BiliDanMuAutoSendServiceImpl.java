package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import cn.hutool.core.thread.NamedThreadFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cuteluobo.livedanmuarchive.async.BiliDanMuSender;
import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.enums.danmu.send.VideoPlatform;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.manager.FileExportManager;
import com.github.cuteluobo.livedanmuarchive.model.DanmuSenderTaskModel;
import com.github.cuteluobo.livedanmuarchive.pojo.BiliDanMuSenderAccountData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuSenderResult;
import com.github.cuteluobo.livedanmuarchive.pojo.biliapi.*;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedPartVideoData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import com.github.cuteluobo.livedanmuarchive.service.database.MainDatabaseService;
import com.github.cuteluobo.livedanmuarchive.utils.BiliDanMuUtil;
import com.github.cuteluobo.livedanmuarchive.utils.BiliLoginUtil;
import com.github.cuteluobo.livedanmuarchive.utils.BiliVideoUtil;
import com.github.cuteluobo.livedanmuarchive.utils.FormatUtil;
import com.github.cuteluobo.livedanmuarchive.utils.reader.BatchSqliteDanMuReader;
import com.github.cuteluobo.livedanmuarchive.utils.reader.SqliteDanMuReader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * B站弹幕发送实现类
 * @author CuteLuoBo
 * @date 2023/3/8 15:00
 */
public class BiliDanMuAutoSendServiceImpl extends BaseDanMuAutoSendService<BiliDanMuSenderAccountData,BiliProcessedVideoData> {

    private final Logger logger = LoggerFactory.getLogger(BiliDanMuAutoSendServiceImpl.class);

    /**
     * 弹幕发送用延迟执行类
     */
    private ExecutorService pool;
    private ScheduledExecutorService scheduledExecutorService;

    private List<BiliDanMuSender> danMuSenderList;
    private BiliProcessedVideoData nowVideoData;

    private CountDownLatch  countDownLatch;

    /**
     * 任务完成监听时间
     */
    private int finishListenSecond = 20;

    /**
     * 设置账户列表并获取实例
     * @param accountList 账户列表
     * @return 实例
     */
    public static BiliDanMuAutoSendServiceImpl getInstance(List<BiliDanMuSenderAccountData> accountList) {
        InstanceClass.INSTANCE.setAccountList(accountList);
        return InstanceClass.INSTANCE;
    }

    /**
     * 单例模式-静态内部类
     */
    private static class InstanceClass {
        private static final BiliDanMuAutoSendServiceImpl INSTANCE = new BiliDanMuAutoSendServiceImpl();
    }


    private BiliDanMuAutoSendServiceImpl() {
        pool = Executors.newCachedThreadPool(new NamedThreadFactory("弹幕发送线程", false));
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("弹幕任务监听线程", false));
        scheduledExecutorService.scheduleWithFixedDelay(createFinishStateTask(), 0, finishListenSecond, TimeUnit.SECONDS);
    }
    /**
     * 检查账号是否登录
     *
     * @param danMuSenderAccountData 弹幕发送者账户数据
     * @return 登录状态
     */
    @Override
    BiliDanMuSenderAccountData checkAccountLogin(BiliDanMuSenderAccountData danMuSenderAccountData) {
        String ck = danMuSenderAccountData.getCookies();
        String acKey = danMuSenderAccountData.getAccessKey();
        BaseUserInfo baseUserInfo = null;
        try {
            if (ck != null) {
                baseUserInfo = BiliLoginUtil.getUserBaseInfoByCk(ck);
                if (baseUserInfo.isLogin()) {
                    danMuSenderAccountData.setLevel(baseUserInfo.getLevel());
                    danMuSenderAccountData.setNickName(baseUserInfo.getNickName());
                    danMuSenderAccountData.setUid(String.valueOf(baseUserInfo.getUid()));
                } else {
                    return null;
                }
            } else if (acKey != null) {
                //TODO 实现调用APP渠道获取用户信息
                boolean login = BiliLoginUtil.checkLoginByAk(acKey);
                if (!login) {
                    return null;
                }
            }
        } catch (Exception e) {
            logger.debug("账户 {} 检查登录状态时出现问题，先行跳过:",danMuSenderAccountData.getUserName(),e);
            return null;
        }
        return danMuSenderAccountData;
    }

    /**
     * 更新账户登录信息(CK/accessKey)
     *
     * @param danMuSenderAccountData 弹幕发送者账户数据
     * @return 更新完成后的账户信息
     */
    @Override
    BiliDanMuSenderAccountData updateAccount(BiliDanMuSenderAccountData danMuSenderAccountData) {
        //TODO 完成账户的登录状态刷新
        return null;
    }

    /**
     * 开始发送弹幕
     */
    @Override
    public void startSendTask(BiliProcessedVideoData biliProcessedVideoData,String fileSaveName) throws ServiceException {
        if (countDownLatch != null && countDownLatch.getCount() > 0) {
            throw new ServiceException("当前仍有任务进行中，请暂停已有任务或等待");
        }
        //检查弹幕存档文件
        List<File> dbList;
        try{
            FileExportManager fileExportManager = FileExportManager.getInstance();
            File saveDir = fileExportManager.getLiveDanMuDir(fileSaveName);
            dbList = fileExportManager.checkDbFileList(saveDir);
        }catch (FileNotFoundException fileNotFoundException) {
            logger.error("获取弹幕文件失败，弹幕发送任务中止,关联弹幕保存名称:{}",fileSaveName,fileNotFoundException);
            return ;
        }
        List<SqliteDanMuReader> danMuReaderList = dbList.stream().map(SqliteDanMuReader::new).collect(Collectors.toList());
        BatchSqliteDanMuReader batchSqliteDanMuReader = new BatchSqliteDanMuReader(danMuReaderList);
        //刷新数据库数据
        //获取分P数据
        List<BiliProcessedPartVideoData> processedPartVideoDataList = biliProcessedVideoData.getPartVideoDataList();
        nowVideoData = biliProcessedVideoData;
        MainDatabaseService mainDatabaseService = MainDatabaseService.getInstance();
        DanmuSenderTaskModel taskModel = mainDatabaseService.getOneLatest(VideoPlatform.BILIBILI.getName(), false, false, biliProcessedVideoData.getCreatorUid(), biliProcessedVideoData.getBvId());
        if (taskModel == null) {
            //TODO 或者监测到任务之后，自动查询其他状态的任务数据并更新状态?
            logger.warn("当前任务{}-{},创建者{} 在数据库未找到已启用的数据，略过数据更新", VideoPlatform.BILIBILI.getName(), biliProcessedVideoData.getBvId(), biliProcessedVideoData.getCreatorUid());
        } else {
            //统计全篇时间
            long firstStartTime = 0;
            long latestEndTime = 0;
            for (BiliProcessedPartVideoData pd :
                    processedPartVideoDataList) {
                if (firstStartTime == 0) {
                    firstStartTime = pd.getVideoStartMillTime();
                } else {
                    firstStartTime = Math.min(firstStartTime, pd.getVideoStartMillTime());
                }
                if (latestEndTime == 0) {
                    latestEndTime = pd.getVideoEndMillTime();
                } else {
                    latestEndTime = Math.max(latestEndTime, pd.getVideoEndMillTime());
                }
            }
            long total =  batchSqliteDanMuReader.countNum(new DanMuDataModelSelector(firstStartTime, latestEndTime));
            //设置预计总数
            taskModel.setDanmuTotalNum(total);
            mainDatabaseService.updateDanMuSenderTask(taskModel);
            //TODO 完成已发送弹幕总数（A.设置一个原子数，传入具体执行线程内统计，通过定时任务写入 B.外部获取执行线程的返回结果，通过定时任务大致估算后写入）
        }
        //设置任务计数器
        countDownLatch = new CountDownLatch(danMuSenderList.size());
        //设置批量数据并执行
        danMuSenderList.forEach(s -> {
            //重置flag
            s.setStop(false);
            s.setFinish(false);
            s.setSqliteDanMuReader(batchSqliteDanMuReader);
            pool.execute(() ->{
                s.createTask(biliProcessedVideoData).run();
                //任务完成后减少计数器
                countDownLatch.countDown();
            });
        });
    }

    /**
     * 创建完成状态查询任务
     * @return 创建的任务
     */
    private Runnable createFinishStateTask() {
        return () -> {
            if (countDownLatch != null && countDownLatch.getCount() == 0) {
                if (nowVideoData != null) {
                    if (danMuSenderList.stream().allMatch(s -> s.isFinish() && !s.isStop())) {
                        MainDatabaseService mainDatabaseService = MainDatabaseService.getInstance();
                        DanmuSenderTaskModel danmuSenderTaskModel = mainDatabaseService.getOneLatest(VideoPlatform.BILIBILI.getName(), false, false, nowVideoData.getCreatorUid(), nowVideoData.getBvId());
                        if (danmuSenderTaskModel != null) {
                            long time = System.currentTimeMillis();
                            danmuSenderTaskModel.setFinishTime(time);
                            danmuSenderTaskModel.setUpdateTime(time);
                            mainDatabaseService.updateDanMuSenderTask(danmuSenderTaskModel);
                            logger.info("{} 弹幕发送任务已完成，耗时{}",danmuSenderTaskModel.getVideoId(), FormatUtil.millTime2String(time-danmuSenderTaskModel.getCreateTime()));
                        }
                    }
                }
            }
        };
    }

    /**
     * 中断发送任务
     *
     * @return 当前任务ID
     */
    @Override
    public int stopSendTask() {
        danMuSenderList.forEach(s->s.setStop(true));
        int onceWaitMillTime = 3000;
        int maxWaitMillTime = onceWaitMillTime * 5;
        try {
            synchronized(this){
                long startTime = System.currentTimeMillis();
                while (!(countDownLatch != null && countDownLatch.getCount() > 0)) {
                    if (System.currentTimeMillis() - startTime > maxWaitMillTime) {
                        logger.error("弹幕发送任务超时仍未关闭，可再次重试或强制中止");
                        return -1;
                    }
                    wait(3000);
                }
                logger.info("弹幕发送中止,弹幕发送结果:\r\n{}",
                        danMuSenderList.stream()
                                .map(BiliDanMuSender::getDanMuSenderResult)
                                .map(DanMuSenderResult::toString)
                                .collect(Collectors.joining("\r\n")));
            }
        }catch (InterruptedException e) {
            logger.error("等待任务结束时,线程中断异常",e);
        }
        return 0;
    }

    /**
     * 恢复发送任务
     *
     * @param taskId 任务ID
     */
    @Override
    public void renewTask(int taskId) {

    }

    /**
     * 设置账户列表，并创建新的弹幕发送者列表和弹幕发送线程池
     * @param accountList 账户列表
     */
    @Override
    public void setAccountList(List<BiliDanMuSenderAccountData> accountList) {
        super.setAccountList(accountList);
        danMuSenderList = accountList.stream().map(BiliDanMuSender::new).collect(Collectors.toList());
//        pool = new ThreadPoolExecutor(0, accountList.size(), 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1),new NamedThreadFactory("弹幕发送线程",false));
    }

    public List<BiliDanMuSender> getDanMuSenderList() {
        return danMuSenderList;
    }

    public void setDanMuSenderList(List<BiliDanMuSender> danMuSenderList) {
        this.danMuSenderList = danMuSenderList;
    }
}
