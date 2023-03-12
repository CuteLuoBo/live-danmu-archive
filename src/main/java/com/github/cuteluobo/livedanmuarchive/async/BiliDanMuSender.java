package com.github.cuteluobo.livedanmuarchive.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.BiliDanMuSenderAccountData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuSenderResult;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedPartVideoData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.BiliProcessedVideoData;
import com.github.cuteluobo.livedanmuarchive.utils.BiliDanMuUtil;
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
     * 最大的额外随机延迟时间(3s)
     */
    private int randomMaxTime = 3000;
    /**
     * 最小的额外随机延迟时间(2s)
     */
    private int randomMinTime = 2000;

    /**
     * 触发发送过快时的延迟时间
     */
    private int fastDelayTime = 0;
    /**
     * 一组弹幕数据内，最大允许的失败尝试次数
     */
    private final int tryMax = 5;

    /**
     * 每页获取的数据
     */
    private final int pageSize = 10;

    /**
     * 触发发送过快的缓存map
     */
    private Map<String, DanMuData> soFastFailMap = new HashMap<>(pageSize);

    private DanMuSenderResult<BiliProcessedVideoData> danMuSenderResult = new DanMuSenderResult<>();

    private BatchSqliteDanMuReader sqliteDanMuReader;
    private BiliProcessedVideoData processedVideoData;

    private Queue<DanMuData> queue = new ArrayDeque<>();
    private boolean stop;

    /**
     * 传入账户数据以初始化
     * @param biliDanMuSenderAccountData  账户数据
     */
    public BiliDanMuSender(BiliDanMuSenderAccountData biliDanMuSenderAccountData) {
        this.accountData = biliDanMuSenderAccountData;
    }

    /**
     * 传入账户数据以初始化
     * @param biliDanMuSenderAccountData  账户数据
     * @param sqliteDanMuReader           批量弹幕数据读取器
     */
    public BiliDanMuSender(BiliDanMuSenderAccountData biliDanMuSenderAccountData,BatchSqliteDanMuReader sqliteDanMuReader) {
        this.accountData = biliDanMuSenderAccountData;
        this.sqliteDanMuReader = sqliteDanMuReader;
    }

    public BiliDanMuSender(BiliDanMuSenderAccountData accountData, DanMuSenderResult<BiliProcessedVideoData> danMuSenderResult,BatchSqliteDanMuReader sqliteDanMuReader) {
        this.accountData = accountData;
        this.danMuSenderResult = danMuSenderResult;
        this.sqliteDanMuReader = sqliteDanMuReader;
    }

    public BiliDanMuSender(BiliDanMuSenderAccountData biliDanMuSenderAccountData, long delayTime, int randomMaxTime,BatchSqliteDanMuReader sqliteDanMuReader) {
        this.accountData = biliDanMuSenderAccountData;
        this.delayTime = delayTime;
        this.randomMaxTime = randomMaxTime;
        this.sqliteDanMuReader = sqliteDanMuReader;
    }

    public BiliDanMuSender(BiliDanMuSenderAccountData accountData, long delayTime, int randomMaxTime, DanMuSenderResult<BiliProcessedVideoData> danMuSenderResult,BatchSqliteDanMuReader sqliteDanMuReader) {
        this.accountData = accountData;
        this.delayTime = delayTime;
        this.randomMaxTime = randomMaxTime;
        this.danMuSenderResult = danMuSenderResult;
        this.sqliteDanMuReader = sqliteDanMuReader;
    }

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
                            accountData.getUserName(),
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
            Map<AtomicInteger, AtomicInteger> partAndPageNumMap = processedVideoData.getPartAndPageNumMap();
            if (partAndPageNumMap == null) {
                logger.warn("传入的视频信息内没有分P和分页缓存数据");
                return;
            }
            //获取分P数据
            List<BiliProcessedPartVideoData> processedPartVideoDataList = processedVideoData.getPartVideoDataList();
            for (Map.Entry<AtomicInteger, AtomicInteger> entry : partAndPageNumMap.entrySet()
            ) {
                int partIndex = entry.getKey().intValue();
                //获取页数并+1，多个线程操作
                int pageNum = entry.getValue().getAndIncrement();
                //防止索引越界
                if (partIndex >= processedPartVideoDataList.size()) {
                    return;
                }
                BiliProcessedPartVideoData processedPartVideoData = processedPartVideoDataList.get(partIndex);
                try {
                    //循环获取并执行
                    do {
                        DanMuDataModelSelector danMuDataModelSelector = new DanMuDataModelSelector(
                                processedPartVideoData.getVideoStartMillTime(),
                                processedPartVideoData.getVideoEndMillTime());
                        List<DanMuData> danMuDataList = sqliteDanMuReader.readListByPage(
                                danMuDataModelSelector,
                                pageNum,
                                pageSize
                        );
                        queue.addAll(danMuDataList);
                        pageNum = entry.getValue().getAndIncrement();
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
                    danMuSenderResult.setLastWorkDataPageNum(pageNum);
                    danMuSenderResult.setLastWorkVideoPartIndex(partIndex);
                    danMuSenderResult.setResidueDataList(new ArrayList<>(queue));
                    logger.error("{}账户弹幕发送任务已中止，原因：{}",accountData.getUserName(),exception.getMessage());
                    return;
                }
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
        while (!queue.isEmpty()) {
            DanMuData danMuData = queue.poll();
            if (!sendDanMu(danMuData,cid,bvId,videoStartTime)) {
                danMuSenderResult.fail();
                failNum++;
            }else{
                danMuSenderResult.success();
            }
            //失败次数过多时，中止
            if ( failNum >= tryMax) {
                logger.warn("失败尝试次数超过上限:{}次，放弃队列内弹幕:\r\n{}",tryMax, queue.stream().map(DanMuData::toNormalString).collect(Collectors.joining(",\r\n")));
                queue.clear();
            }
            //延时等待
            try {
                Thread.sleep(delayTime + Math.max(new Random().nextInt(randomMaxTime), randomMinTime) + fastDelayTime);
            } catch (InterruptedException e) {
                logger.error("{}账户延时发送弹幕线程异常中断", accountData.getUserName(), e);
            }
            //设置的中止标识
            if (stop) {
                throw new InterruptedException("检测到中止标识");
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
    private boolean sendDanMu(DanMuData danMuData,long cid,String bvId,long videoStartTime) throws ServiceException {
        HttpResponse<String> httpResponse;
        try {
            httpResponse = BiliDanMuUtil.sendDanMu(
                    cid,
                    danMuData.getContent(),
                    bvId,
                    0,
                    danMuData.getTimestamp() - videoStartTime,
                    danMuData.getDanMuFormatData().getFontColor(),
                    (float) danMuData.getDanMuFormatData().getFontSize(),
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
                    logger.info("{}账户发送弹幕成功，{}:{}", accountData.getUserName(),danMuData.getUserIfo()==null?"?":danMuData.getUserIfo().getNickName(),danMuData.getContent());
                    //成功发送时，降低延迟时间
                    if (fastDelayTime > 0) {
                        fastDelayTime = Math.max(0, fastDelayTime-randomMinTime);
                    }
                    return true;
                } else {
                    JsonNode messageNode = jsonNode.get("message");
                    logger.error("{}账户发送弹幕\"{}\"出现问题：code:{},message:{}", accountData.getUserName(),danMuData.getContent(), code, messageNode == null ? "(api未返回消息)" : messageNode.asText());
                    //弹幕发送过快
                    if (code == 36703) {
                        //消息不相同时，保存到map中一次，下次再出现相同内容时，直接跳过发送
                        if (soFastFailMap.get(danMuData.getContent()) == null) {
                            //发送失败的消息补回队列
                            queue.add(danMuData);
                            soFastFailMap.put(danMuData.getContent(), danMuData);
                        }
                        //增加延迟时间
                        fastDelayTime += randomMinTime * 2;
                    } else {
                        //发送失败的消息补回队列
                        queue.add(danMuData);
                    }
                    if (code < 0) {
                        danMuSenderResult.fail();
                        throw new ServiceException(String.format("%s账户已中断，待重试", accountData.getUserName()));
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
}
