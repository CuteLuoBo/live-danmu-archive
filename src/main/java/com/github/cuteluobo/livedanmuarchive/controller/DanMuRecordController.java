package com.github.cuteluobo.livedanmuarchive.controller;

import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlSequence;
import com.github.cuteluobo.livedanmuarchive.builder.DanMuServiceBuilder;
import com.github.cuteluobo.livedanmuarchive.enums.*;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.listener.impl.DanMuClientStopListener;
import com.github.cuteluobo.livedanmuarchive.manager.DanMuClientEventManager;
import com.github.cuteluobo.livedanmuarchive.service.DanMuService;
import com.github.cuteluobo.livedanmuarchive.utils.CustomConfigUtil;
import org.java_websocket.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * 弹幕录制控制器
 * @author CuteLuoBo
 * @date 2022/4/8 10:55
 */
public class DanMuRecordController {
    Logger logger = LoggerFactory.getLogger(DanMuRecordController.class);

    /**
     * 任务映射表
     * 第一层：直播平台-当前平台下运行的任务列表
     * 第二层 直播录制名-具体执行Service实例
     */
    private Map<WebsiteType, Map<String,DanMuService>> danMuTaskMap;

    /**
     * 任务重试时间映射表
     */
    private Map<String, Long> danMuTaskRetryTimeMap = null;;


    /**
     * 等待启动线程的队列
     */
    private List<DanMuService> danMuServicesWaitQueue = null;;

    /**
     * 延迟执行线程池
     */
    private ScheduledExecutorService pool;

    /**
     * 弹幕事件管理器
     */
    private DanMuClientEventManager danMuClientEventManager = null;

    //使用单实例模式

    public static DanMuRecordController instance = new DanMuRecordController();

    private DanMuRecordController () {
        //初始化监听
        danMuClientEventManager = new DanMuClientEventManager();
        DanMuClientStopListener danMuClientStopListener = new DanMuClientStopListener();
        //监听ERROR会导致重复执行问题
//        danMuClientEventManager.subscribe(DanMuClientEventType.ERROR, danMuClientStopListener);
        danMuClientEventManager.subscribe(DanMuClientEventType.CLOSE, danMuClientStopListener);

        //初始化缓存map/list
        danMuTaskRetryTimeMap = new HashMap<>(10);
        danMuServicesWaitQueue = new ArrayList<>(10);
        danMuTaskMap = new HashMap<>(WebsiteType.values().length);
        //线程池创建参考：https://blog.csdn.net/sinat_36710456/article/details/107221342
        //获取当前系统最大线程数
        int nThreads = Runtime.getRuntime().availableProcessors();
        //线程命名工厂
        ThreadFactory threadFactory = new NamedThreadFactory("danmu-record-task");
        //延迟执行线程池
        pool = Executors.newScheduledThreadPool(nThreads,threadFactory);
//        pool = new ThreadPoolExecutor(nThreads+1, 200, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(1024),threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static DanMuRecordController getInstance() {
        return instance;
    }


    /**
     * 添加任务
     * @param liveRoomUrl 直播间url
     * @param saveName 保存名称
     * @param danMuExportType    弹幕导出类型
     * @param danMuExportPattern 弹幕导出保存模式
     * @param retryTime 重试时间(秒)
     * @throws ServiceException
     * @throws IOException
     */
    public void addTask(String liveRoomUrl, String saveName, DanMuExportType danMuExportType, ExportPattern danMuExportPattern, Long retryTime) throws ServiceException, IOException {
        DanMuServiceBuilder danMuServiceBuilder = new DanMuServiceBuilder(liveRoomUrl);
        danMuServiceBuilder.saveName(saveName).danMuExportType(danMuExportType).danMuExportPattern(danMuExportPattern).danMuClientEventManager(danMuClientEventManager);
        DanMuService danMuService = danMuServiceBuilder.build();
        //存入映射记录
        Map<String, DanMuService> liveNameServiceMap = danMuTaskMap.computeIfAbsent(danMuServiceBuilder.getWebsiteType(), k -> new HashMap<>(10));
        //任务名称
        String taskName = createTaskName(danMuService.getServiceSupportWebsiteType(),danMuService.getLiveRoomCode(),saveName);
        liveNameServiceMap.put(taskName, danMuService);
        //储存任务自动重试时间
        danMuTaskRetryTimeMap.put(taskName, retryTime);
        //启动线程
        pool.execute(() ->{
            try {
                logger.info("尝试执行弹幕录制任务: {} ,url: {} ",saveName,liveRoomUrl);
                danMuService.startRecord();
            } catch (Exception e) {
                logger.error("弹幕录制任务:{},执行错误,堆栈信息：{}",taskName,e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * 根据默认配置文件添加任务
     */
    public void addTaskByNormalConfigFile() {
        logger.info("正在尝试从配置文件中获取录制任务...");
        //读取配置文件
        CustomConfigUtil customConfigUtil = CustomConfigUtil.INSTANCE;
        YamlMapping allConfig = customConfigUtil.getConfigMapping();
        YamlMapping recordConfig = allConfig.yamlMapping(ConfigRecordField.MAIN_FIELD.getFieldString());
        //遍历配置录制列表
        YamlSequence recordList = recordConfig.yamlSequence(ConfigRecordField.RECORD_LIST.getFieldString());
        for (int i = 0; i < recordList.size(); i++) {
            YamlMapping recordUnit = recordList.yamlMapping(i);
            logger.info("读取到第{}个任务，尝试解析",i+1);
            try {
                String roomUrl = recordUnit.string(ConfigRecordField.ROOM_URL.getFieldString());
                String saveName = recordUnit.string(ConfigRecordField.SAVE_NAME.getFieldString());
                String danMuExportTypeString = recordUnit.string(ConfigRecordField.DANMU_EXPORT_TYPE.getFieldString());
                DanMuExportType danMuExportType = DanMuExportType.getEnumByValue(danMuExportTypeString);
                String exportPatternString = recordUnit.string(ConfigRecordField.EXPORT_PATTERN.getFieldString());
                ExportPattern exportPattern = ExportPattern.getEnumByText(exportPatternString);
                long retryTime = recordUnit.longNumber(ConfigRecordField.DANMU_RECORD_RETRY_TIME.getFieldString());
                logger.info("第{}个任务：{}，解析完成，尝试执行",i+1,saveName);
                addTask(roomUrl, saveName, danMuExportType, exportPattern, retryTime);
            } catch (Exception e) {
                logger.error("读取配置文件中的第{}个录制任务错误:",i+1,e);
            }
        }
    }

    /**
     * 重启任务（用于监听器执行）
     * @param websiteType 直播平台类型
     * @param liveRoomCode 直播间房间代号
     */
    public void restartTask(WebsiteType websiteType,String liveRoomCode,String saveName) {
        String taskName = createTaskName(websiteType, liveRoomCode,saveName);
        long retryTime = danMuTaskRetryTimeMap.get(taskName);
        if (retryTime == -1) {
            logger.info("任务{}的重试时间设置为{}，跳过重试",taskName,retryTime);
            return;
        }
        Map<String, DanMuService> danMuServiceMap = danMuTaskMap.get(websiteType);
        if (danMuServiceMap == null) {
            logger.error("当前直播平台{}没有已初始化可重试的弹幕任务，跳过重试",websiteType.getName());
            return;
        }
        DanMuService danMuService = danMuServiceMap.get(taskName);
        if (danMuService == null) {
            logger.error("当前已初始化任务列表中没有对应{}任务，跳过重试，当前的列表：{}",taskName, String.join("，\\r\\n", danMuServiceMap.keySet()));
            return;
        }
        logger.info("任务：{}，将在{}秒后进行重试",taskName,retryTime);
        pool.schedule(() -> {
            try {
                logger.info("任务：{}，进行重试.....",taskName);
                danMuService.startRecord();
            } catch (Exception e) {
                //TODO 解决抛出错误时，无法正常重试的问题（改造内部/外部增加通知事件）
                logger.error("弹幕录制任务:{},执行错误,堆栈信息：{}",taskName,e.getMessage());
                e.printStackTrace();
            }
        }, retryTime,TimeUnit.SECONDS);
    }

    /**
     * 可能会用到的启动队列(?)
     */
    private void startThreadQueue() {
        Iterator<DanMuService> danMuServiceIterator = danMuServicesWaitQueue.iterator();
        while (danMuServiceIterator.hasNext()) {
            DanMuService danMuService = danMuServiceIterator.next();
            pool.execute(()->{
            });
            danMuServiceIterator.remove();
        }
    }

    /**
     * 任务命名
     * 模式：{直播间平台}-{直播间代号}
     * @param websiteType 直播间平台类型
     * @param liveRoomCode 直播间代号
     * @return
     */
    private String createTaskName(WebsiteType websiteType, String liveRoomCode,String saveName) {
        return saveName + "-" + liveRoomCode;
    }
}
