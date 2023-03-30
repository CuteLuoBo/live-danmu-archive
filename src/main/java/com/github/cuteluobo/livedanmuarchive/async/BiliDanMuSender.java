package com.github.cuteluobo.livedanmuarchive.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cuteluobo.livedanmuarchive.dto.DanMuAccountTaskSelector;
import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.enums.danmu.send.VideoPlatform;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.model.DanmuAccountTaskModel;
import com.github.cuteluobo.livedanmuarchive.model.DanmuSenderTaskModel;
import com.github.cuteluobo.livedanmuarchive.pojo.BiliDanMuSenderAccountData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuSenderResult;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedPartVideoData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import com.github.cuteluobo.livedanmuarchive.service.database.MainDatabaseService;
import com.github.cuteluobo.livedanmuarchive.utils.BiliDanMuUtil;
import com.github.cuteluobo.livedanmuarchive.utils.FormatUtil;
import com.github.cuteluobo.livedanmuarchive.utils.reader.BatchSqliteDanMuReader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 弹幕发送者异步任务
 * @author CuteLuoBo
 * @date 2023/3/10 15:06
 */
public class BiliDanMuSender{
    private final Logger logger = LoggerFactory.getLogger(BiliDanMuSender.class);

    private BiliDanMuSenderAccountData accountData;

    private boolean accountError = false;

    /**
     * 发送延迟(ms),默认为5000ms=5s
     */
    private long delayTime = 6000;
    /**
     * 最大的额外随机延迟时间(4s)
     */
    private int randomMaxTime = 4000;
    /**
     * 最小的额外随机延迟时间(2s)
     */
    private int randomMinTime = 2000;

    /**
     * 随机区间
     */
    private int randomRange = randomMaxTime - randomMinTime;

    /**
     * 触发发送过快时的延迟时间
     */
    private int fastDelayTime = 0;


    /**
     * 每页获取的数据
     */
    private final int pageSize = 10;
    /**
     * 一组弹幕数据内，最大允许的失败尝试次数
     */
    private final int tryMax = pageSize;

    /**
     * 触发发送过快的缓存map
     */
    private Map<String, DanMuData> soFastFailMap = new HashMap<>(pageSize);

    private DanMuSenderResult<BiliProcessedVideoData> danMuSenderResult = new DanMuSenderResult<>();

    private BatchSqliteDanMuReader sqliteDanMuReader;
    private BiliProcessedVideoData processedVideoData;

    private Queue<DanMuData> queue = new ArrayDeque<>();
    private boolean stop;

    private boolean finish;

    private Random random = new Random();

    /**
     * 允许彩色弹幕
     */
    private boolean allowColor = false;
    /**
     * 允许更多的弹幕长度(level1<20)
     */
    private boolean allowMoreText = false;
    /**
     * 允许底部弹幕
     */
    private boolean allowBottomDanMu = false;
    /**
     * 允许顶部弹幕
     */
    private boolean allowTopDanMu = false;
    /**
     * 允许高级弹幕
     */
    private boolean allowExpertDanMu = false;

    /**
     * 传入账户数据以初始化
     * @param biliDanMuSenderAccountData  账户数据
     */
    public BiliDanMuSender(BiliDanMuSenderAccountData biliDanMuSenderAccountData) {
        this.accountData = biliDanMuSenderAccountData;
        setDanMuPermission(accountData.getLevel());
    }

    /**
     * 传入账户数据以初始化
     * @param biliDanMuSenderAccountData  账户数据
     * @param sqliteDanMuReader           批量弹幕数据读取器
     */
    public BiliDanMuSender(BiliDanMuSenderAccountData biliDanMuSenderAccountData,BatchSqliteDanMuReader sqliteDanMuReader) {
        this.accountData = biliDanMuSenderAccountData;
        this.sqliteDanMuReader = sqliteDanMuReader;
        setDanMuPermission(accountData.getLevel());
    }

    public BiliDanMuSender(BiliDanMuSenderAccountData accountData, DanMuSenderResult<BiliProcessedVideoData> danMuSenderResult,BatchSqliteDanMuReader sqliteDanMuReader) {
        this.accountData = accountData;
        this.danMuSenderResult = danMuSenderResult;
        this.sqliteDanMuReader = sqliteDanMuReader;
        setDanMuPermission(accountData.getLevel());
    }

    public BiliDanMuSender(BiliDanMuSenderAccountData biliDanMuSenderAccountData, long delayTime, int randomMaxTime,BatchSqliteDanMuReader sqliteDanMuReader) {
        this.accountData = biliDanMuSenderAccountData;
        this.delayTime = delayTime;
        this.randomMaxTime = randomMaxTime;
        this.sqliteDanMuReader = sqliteDanMuReader;
        setDanMuPermission(accountData.getLevel());
    }

    public BiliDanMuSender(BiliDanMuSenderAccountData accountData, long delayTime, int randomMaxTime, DanMuSenderResult<BiliProcessedVideoData> danMuSenderResult,BatchSqliteDanMuReader sqliteDanMuReader) {
        this.accountData = accountData;
        this.delayTime = delayTime;
        this.randomMaxTime = randomMaxTime;
        this.danMuSenderResult = danMuSenderResult;
        this.sqliteDanMuReader = sqliteDanMuReader;
        setDanMuPermission(accountData.getLevel());
    }

    /**
     * 设置弹幕权限
     * @param level 账户等级
     */
    public void setDanMuPermission(int level) {
        if (level > 1) {
            allowColor = true;
            allowExpertDanMu = true;
            allowMoreText = true;
            if (level > 2) {
                allowTopDanMu = true;
                allowBottomDanMu = true;
            }
        }
    }

    /**
     * 设置视频数据
     * @param processedVideoData  视频数据
     */
    public void setVideoData(BiliProcessedVideoData processedVideoData) {
        this.processedVideoData = processedVideoData;
    }

    /**
     * 创建弹幕补发任务，一般用于其他账户异常中止时进行调用，当手动中止时，弹幕队列将被丢弃而不保存
     * @param oldDanMuSenderResult  旧的弹幕发送数据（包含异常中止）
     * @return 创建的任务
     */
    public Runnable createRenewTask(DanMuSenderResult<BiliProcessedVideoData> oldDanMuSenderResult) {
        return ()->{
            List<DanMuData> danMuDataList = oldDanMuSenderResult.getResidueDataList();
            BiliProcessedVideoData biliProcessedVideoData = oldDanMuSenderResult.getProcessedVideoData();
            List<BiliProcessedPartVideoData> partDataList = biliProcessedVideoData.getPartVideoDataList();
            int lastPartIndex = oldDanMuSenderResult.getLastWorkVideoPartIndex();
            if (lastPartIndex < partDataList.size()) {
                BiliProcessedPartVideoData biliProcessedPartVideoData = partDataList.get(lastPartIndex);
                Queue<DanMuData> queue = new ArrayDeque<>(danMuDataList);
                try {
                    runDanMuBatchSender(queue,
                            biliProcessedPartVideoData.getCid(), biliProcessedVideoData.getBvId(), biliProcessedPartVideoData.getVideoStartMillTime());
                }
                //捕获发送时的错误消息
                catch (ServiceException | InterruptedException exception) {
                    //设置为中断并设置结果消息
                    if (exception instanceof ServiceException) {
                        accountError = true;
                    }
                    logger.error("{}账户弹幕补发任务已中止，队列将抛弃:{}，原因：{}",
                            accountData.getNickName(),
                            queue.stream().map(DanMuData::toNormalString).collect(Collectors.joining(",\r\n")),
                            exception.getMessage());
                }
            }
        };

    }

    /**
     * 创建发送任务，必须先传入sqliteDanMuReader，否则会跳过
     * @param processedVideoData 传入的处理过的视频数据
     * @return 此对象实例
     */
    public Runnable createTask(@NotNull BiliProcessedVideoData processedVideoData) {
        return () -> {
            if (sqliteDanMuReader == null) {
                logger.error("未传入可获取的弹幕获取对象，跳过任务");
                return;
            }
            danMuSenderResult.setProcessedVideoData(processedVideoData);
            List<AtomicInteger> videoPageIndexList = processedVideoData.getPageIndexList();
            if (videoPageIndexList == null) {
                logger.warn("传入的视频信息内没有分P缓存数据");
                return;
            }
            MainDatabaseService mainDatabaseService = MainDatabaseService.getInstance();
            //获取当前进行的任务
            DanmuSenderTaskModel taskModel = mainDatabaseService.getOneLatest(VideoPlatform.BILIBILI.getName(), false, false, processedVideoData.getCreatorUid(), processedVideoData.getBvId());
            DanmuAccountTaskModel danmuAccountTaskModel = null;
            boolean newCreate = false;
            if (taskModel != null) {
                danmuAccountTaskModel = mainDatabaseService.getAccountTaskByNoFinish(taskModel.getId(), accountData.getUid(), processedVideoData.getBvId(), false);
                if (danmuAccountTaskModel == null) {
                    //当前发送者没有结果时，查询当前BV相关的所有记录
                    DanMuAccountTaskSelector selector = new DanMuAccountTaskSelector();
                    selector.setDanmuSenderTaskId(taskModel.getId());
                    selector.setVideoId(processedVideoData.getBvId());
                    selector.setStop(false);
                    List<DanmuAccountTaskModel> taskModelList = mainDatabaseService.getAccountTaskListBySelector(selector);
                    //获取进度最后的任务
                    for (DanmuAccountTaskModel m : taskModelList
                    ) {
                        //只读取未完成的任务
                        if (m.getFinishTime() == null) {
                            if (danmuAccountTaskModel == null) {
                                danmuAccountTaskModel = m;
                            } else if (m.getLastVideoPartIndex() >= danmuAccountTaskModel.getLastVideoPartIndex() && m.getPageIndex() >= danmuAccountTaskModel.getPageIndex()) {
                                danmuAccountTaskModel = m;
                            }
                        }
                    }
                    //仍没有记录时，新建对象
                    if (danmuAccountTaskModel == null) {
                        //有完成记录时，说明全部任务已完成，跳过
                        selector.setStartFinishTime(0L);
                        if (!mainDatabaseService.getAccountTaskListBySelector(selector).isEmpty()) {
                            logger.info("当前 {} 视频已有弹幕发送完成记录，任务可能已完成，跳过当前账户 {} ({})发送", processedVideoData.getBvId(), accountData.getNickName(), accountData.getUid());
                            finish = true;
                            return;
                        }
                        //否则创建新的任务记录
                        danmuAccountTaskModel = new DanmuAccountTaskModel();
                        danmuAccountTaskModel.setDanmuSenderTaskId(taskModel.getId());
                        danmuAccountTaskModel.setSenderUid(accountData.getUid());
                        danmuAccountTaskModel.setVideoId(processedVideoData.getBvId());
                        danmuAccountTaskModel.setLastVideoPartIndex(0);
                        danmuAccountTaskModel.setCreateTime(System.currentTimeMillis());
                        newCreate = true;
                    } else {
                        //从其他账户获取结果时，视为新建记录
                        danmuAccountTaskModel.setId(null);
                        danmuAccountTaskModel.setPageIndex(danmuAccountTaskModel.getPageIndex() + 1);
                        danmuAccountTaskModel.setSenderUid(accountData.getUid());
                    }
                }
            } else {
                logger.warn("未找到对应的弹幕主任务，跳过弹幕数据保存");
            }
            //获取分P数据
            List<BiliProcessedPartVideoData> processedPartVideoDataList = processedVideoData.getPartVideoDataList();
            int startVideoIndex = 0;
            int firstSkipIndex = 0;
            //根据数据库储存的已有数据，更新任务进度（分P索引和记录分P索引）
            if (danmuAccountTaskModel != null && !newCreate) {
                if (startVideoIndex < videoPageIndexList.size()) {
                    startVideoIndex = danmuAccountTaskModel.getLastVideoPartIndex();
                }
                AtomicInteger pageIndex = videoPageIndexList.get(startVideoIndex);
                if (pageIndex.get() == 0) {
                    pageIndex.set(danmuAccountTaskModel.getPageIndex());
                }
                firstSkipIndex = danmuAccountTaskModel.getLastDanmuIndex();
            }
            for (int i = startVideoIndex; i < videoPageIndexList.size(); i++) {
                DanMuSenderResult<BiliProcessedVideoData> danMuSenderResultClone = danMuSenderResult.clone();
                long partStartTime = System.currentTimeMillis();
                //获取页数并+1，多个线程操作
                int pageNowIndex =  videoPageIndexList.get(i).getAndIncrement();
                logger.debug("nowPartIndex:{},nowPageNum:{},AtoMicLong:{}",i,pageNowIndex,videoPageIndexList.get(i).get());
                //防止索引越界
                if (i >= processedPartVideoDataList.size()) {
                    break;
                }
                BiliProcessedPartVideoData processedPartVideoData = processedPartVideoDataList.get(i);
                logger.info("{}账户任务：尝试发送弹幕，{}视频的第 {} P,标题：{}",accountData.getNickName(),processedVideoData.getBvId(),i+1,processedPartVideoData.getPartName());
                try {
                    DanMuDataModelSelector danMuDataModelSelector;
                    //循环获取并执行
                    do {

                        danMuDataModelSelector = new DanMuDataModelSelector(
                                processedPartVideoData.getVideoStartMillTime(),
                                processedPartVideoData.getVideoEndMillTime());
                        List<DanMuData> danMuDataList = sqliteDanMuReader.readListByPage(
                                danMuDataModelSelector,
                                pageNowIndex,
                                pageSize
                        );

                        //从数据库读取
                        if (firstSkipIndex > 0) {
                            queue.addAll(danMuDataList.subList(Math.max(danMuDataList.size(),firstSkipIndex), danMuDataList.size()));
                            firstSkipIndex = 0;
                        } else {
                            queue.addAll(danMuDataList);
                        }

                        if (danmuAccountTaskModel != null) {
                            //设置数据保存
                            danmuAccountTaskModel.setLastVideoPartCid(processedPartVideoData.getCid());
                            danmuAccountTaskModel.setLastVideoPartIndex(i);
                            danmuAccountTaskModel.setPageSize(pageSize);
                            danmuAccountTaskModel.setPageIndex(pageNowIndex);
                            danmuAccountTaskModel.setLastDanmuIndex(0);
                            danmuAccountTaskModel.setUpdateTime(System.currentTimeMillis());
                            //新建或更新
                            if (danmuAccountTaskModel.getId() == null) {
                                mainDatabaseService.addAccountTask(danmuAccountTaskModel);
                            } else {
                                mainDatabaseService.updateAccountTask(danmuAccountTaskModel);
                            }
                            danmuAccountTaskModel = mainDatabaseService.getAccountTaskByNoFinish(taskModel.getId(), accountData.getUid(), processedVideoData.getBvId(), false);
                        }
                        pageNowIndex = videoPageIndexList.get(i).getAndIncrement();
                        logger.debug("nowPageNum:{},AtoMicLong:{}",pageNowIndex,videoPageIndexList.get(i).get());
                    }
                    while (!runDanMuBatchSender(queue,
                            processedPartVideoData.getCid(), processedVideoData.getBvId(), processedPartVideoData.getVideoStartMillTime()));
                }
                //捕获发送时的错误消息
                catch (ServiceException | InterruptedException exception) {
                    //设置为中断并设置结果消息
                    if (exception instanceof ServiceException) {
                        accountError = true;
                    }
                    danMuSenderResult.setLastWorkDataPageNum(pageNowIndex);
                    danMuSenderResult.setLastWorkVideoPartIndex(i);
                    danMuSenderResult.setResidueDataList(new ArrayList<>(queue));
                    if (danmuAccountTaskModel != null) {
                        //设置数据保存
                        danmuAccountTaskModel.setLastVideoPartCid(processedPartVideoData.getCid());
                        danmuAccountTaskModel.setLastVideoPartIndex(i);
                        danmuAccountTaskModel.setPageSize(pageSize);
                        danmuAccountTaskModel.setPageIndex(pageNowIndex);
                        danmuAccountTaskModel.setLastDanmuIndex(queue.size());
                        danmuAccountTaskModel.setUpdateTime(System.currentTimeMillis());
                        //新建或更新
                        if (danmuAccountTaskModel.getId() == null) {
                            mainDatabaseService.addAccountTask(danmuAccountTaskModel);
                        } else {
                            mainDatabaseService.updateAccountTask(danmuAccountTaskModel);
                        }
                    }
                    logger.error("{}账户，弹幕发送任务已中止，原因：{}",accountData.getNickName(),exception.getMessage());
                    return;
                }
                logger.info("{}账户任务：当P弹幕发送完成，耗时{}，分配弹幕总数:{}，成功次数:{}，失败次数:{}，{}视频的第 {} P,标题：{}",
                        accountData.getNickName(),
                        FormatUtil.millTime2String(System.currentTimeMillis()-partStartTime),
                        danMuSenderResult.getTotal().get()-danMuSenderResultClone.getTotal().get(),
                        danMuSenderResult.getSuccessNum().get()-danMuSenderResultClone.getSuccessNum().get(),
                        danMuSenderResult.getFailNum().get()-danMuSenderResultClone.getFailNum().get(),
                        processedVideoData.getBvId(),
                        i+1,
                        processedPartVideoData.getPartName());
            }
            //所有弹幕发送完成时，更新完成时间
            if (danmuAccountTaskModel != null) {
                danmuAccountTaskModel.setUpdateTime(System.currentTimeMillis());
                danmuAccountTaskModel.setFinishTime(System.currentTimeMillis());
                mainDatabaseService.updateAccountTask(danmuAccountTaskModel);
                finish = true;
            }
        };
    }


    /**
     * 执行弹幕批量发送任务
     * @param queue  本次任务的数据队列
     * @param cid            分P CID
     * @param bvId           稿件bvid
     * @param videoStartTime 当前P视频起始时间
     * @return 结束循环flag
     * @throws ServiceException 发送账户错误时
     * @throws InterruptedException 中止标识中断时
     */
    private boolean runDanMuBatchSender(Queue<DanMuData> queue,long cid,String bvId,long videoStartTime) throws ServiceException, InterruptedException {
        //清空过快发送缓存
        soFastFailMap.clear();
        //当此P的弹幕获取完成时，跳转到下一P
        if (queue.isEmpty()) {
            return true;
        }
        int failNum = 0;
        //循环获取队列中的弹幕数据
        String lastSendContent = "";
        while (!queue.isEmpty()) {
            DanMuData danMuData = queue.poll();
            //当内容相同，且有更多可用值时，先跳过一个
            if (queue.size() > 1 && lastSendContent.equals(danMuData.getContent())) {
                queue.add(danMuData);
                danMuData = queue.poll();
            }
            if (danMuData != null && danMuData.getContent() != null) {
                if (!allowMoreText && danMuData.getContent().length() > 20) {
                    logger.warn("当前{}账户，等级过低，跳过20长度以上的弹幕，弹幕消息：{}:{}",
                            accountData.getNickName(),
                            danMuData.getUserIfo() == null ? "?" : danMuData.getUserIfo().getNickName(),
                            danMuData.getContent());
                }
                //发送并统计结果
                Boolean senderResult = sendDanMu(danMuData, cid, bvId, videoStartTime);
                if (senderResult != null) {
                    if (senderResult) {
                        danMuSenderResult.success();
                    } else {
                        danMuSenderResult.fail();
                        failNum++;
                    }
                }
                lastSendContent = danMuData.getContent();
                //失败次数过多时，中止
                if (failNum >= tryMax) {
                    logger.warn("失败尝试次数超过上限:{}次，放弃队列内弹幕:\r\n{}", tryMax, queue.stream().map(DanMuData::toNormalString).collect(Collectors.joining(",\r\n")));
                    queue.clear();
                }
                //延时等待
                try {
                    Thread.sleep(delayTime + randomMinTime + random.nextInt(randomRange) + fastDelayTime);
                } catch (InterruptedException e) {
                    logger.error("{}账户，延时发送弹幕线程异常中断", accountData.getNickName(), e);
                }
                //设置的中止标识
                if (stop) {
                    throw new InterruptedException("检测到中止标识");
                }
            }
        }
        return false;
    }

    /**
     * 执行单次弹幕发送任务
     * @param danMuData 弹幕数据
     * @param cid            视频分P的CID
     * @param bvId           视频稿件的BVID
     * @param videoStartTime 视频初始时间
     * @return 是否发送成功
     * @throws ServiceException 出现账户异常时
     */
    private Boolean sendDanMu(DanMuData danMuData,long cid,String bvId,long videoStartTime) throws ServiceException {
        HttpResponse<String> httpResponse;
        try {
            httpResponse = BiliDanMuUtil.sendDanMu(
                    cid,
                    danMuData.getContent(),
                    bvId,
                    0,
                    danMuData.getTimestamp() - videoStartTime,
                    allowColor?danMuData.getDanMuFormatData().getFontColor():16777215,
                    allowColor?(float) danMuData.getDanMuFormatData().getFontSize():25,
                    0,
                    1, accountData.getCookies(), accountData.getAccessKey());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.debug("请求异常中断", e);
            return false;
        }
        //解析发送结果
        String bodyString = httpResponse.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(bodyString);
        } catch (JsonProcessingException e) {
            logger.error("解析返回消息失败,消息原文：{}",bodyString,e);
            queue.add(danMuData);
            return false;
        }
        if (jsonNode != null) {
            JsonNode codeNode = jsonNode.get("code");
            //判断发送是否成功
            if (codeNode != null) {
                int code = codeNode.asInt();
                if (code == 0) {
                    logger.debug("{}账户，发送弹幕成功，({}:){}", accountData.getNickName(),danMuData.getUserIfo()==null?"?":danMuData.getUserIfo().getNickName(),danMuData.getContent());
                    //成功发送时，降低延迟时间
                    if (fastDelayTime > 0) {
                        fastDelayTime = Math.max(0, fastDelayTime - randomMinTime);
                    }
                    return true;
                } else {
                    JsonNode messageNode = jsonNode.get("message");
                    logger.error("{}账户，发送弹幕({}:)\"{}\"出现问题：code:{},message:{}", accountData.getNickName(),danMuData.getUserIfo()==null?"?":danMuData.getUserIfo().getNickName(),danMuData.getContent(), code, messageNode == null ? "(api未返回消息)" : messageNode.asText());
                    //弹幕发送过快
                    if (code == 36703) {
                        //消息不相同时，保存到map中一次，下次再出现相同内容时，直接跳过发送
                        if (soFastFailMap.get(danMuData.getContent()) == null) {
                            soFastFailMap.put(danMuData.getContent(), danMuData);
                            //发送失败的消息补回队列
                            queue.add(danMuData);
                            return null;
                        } else{
                            //首次相同内容且未添加过昵称时，再次尝试添加昵称发送
                            if (!danMuData.getContent().contains(danMuData.getUserIfo().getNickName())) {
                                //添加昵称避免重复
                                danMuData.setContent(danMuData.getUserIfo().getNickName() + " : " + danMuData.getContent());
                                //发送失败的消息补回队列
                                queue.add(danMuData);
                                return null;
                            }
                        }
                        //增加延迟时间
                        fastDelayTime += randomMinTime * 2;
                    } else {
                        //发送失败的消息补回队列
                        queue.add(danMuData);
                    }
                    if (code < 0) {
                        danMuSenderResult.fail();
                        throw new ServiceException(String.format("%s账户已中断，待重试", accountData.getNickName()));
                    }
                    return false;
                }
            }
        }
        return false;
    }

    public BiliDanMuSenderAccountData getAccountData() {
        return accountData;
    }

    public void setAccountData(BiliDanMuSenderAccountData accountData) {
        this.accountError = false;
        this.accountData = accountData;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    public int getRandomMaxTime() {
        return randomMaxTime;
    }

    public void setRandomMaxTime(int randomMaxTime) {
        this.randomMaxTime = randomMaxTime;
    }

    public boolean isAccountError() {
        return accountError;
    }

    public DanMuSenderResult<BiliProcessedVideoData> getDanMuSenderResult() {
        return danMuSenderResult;
    }

    public void setDanMuSenderResult(DanMuSenderResult<BiliProcessedVideoData> danMuSenderResult) {
        this.danMuSenderResult = danMuSenderResult;
    }

    public void setAccountError(boolean accountError) {
        this.accountError = accountError;
    }


    public BiliProcessedVideoData getProcessedVideoData() {
        return processedVideoData;
    }

    public void setProcessedVideoData(BiliProcessedVideoData processedVideoData) {
        this.processedVideoData = processedVideoData;
    }

    public Queue<DanMuData> getQueue() {
        return queue;
    }

    public BatchSqliteDanMuReader getSqliteDanMuReader() {
        return sqliteDanMuReader;
    }

    public void setSqliteDanMuReader(BatchSqliteDanMuReader sqliteDanMuReader) {
        this.sqliteDanMuReader = sqliteDanMuReader;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

}
