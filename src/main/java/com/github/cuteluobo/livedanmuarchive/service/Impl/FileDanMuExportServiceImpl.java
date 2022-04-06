package com.github.cuteluobo.livedanmuarchive.service.Impl;

import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author CuteLuoBo
 * @date 2022/2/11 16:15
 */
public class FileDanMuExportServiceImpl extends ConsoleDanMuExportServiceImpl {

    Logger logger = LoggerFactory.getLogger(FileDanMuExportServiceImpl.class);
    /**
     * 默认储存路径
     */
    private File savePath;
    /**
     * 文件分割分钟，默认60分钟
     */
    private int fileSplitMinutes = 60;
    /**
     * 当前文件开始记录时的时间戳
     */
    private long startRecordTimeStamp = 0L;
    /**
     * 文件名前缀
     */
    private String fileNamePrefix = "弹幕记录";
    /**
     * 时间导出格式
     */
    private String timeExportFormat = "MM-dd HH:mm:ss";

    /**
     * 文件名后缀
     */
    private String fileNameSuffix = ".json";
    //TODO 增加不同导出文件的分岔接口，完全拆分为单独导出类/由主导出类调用接口

    public FileDanMuExportServiceImpl(File savePath) {
        super();

        if (savePath == null) {
            this.savePath = new File("export/");
        } else {
            this.savePath = savePath;
        }

        if (!savePath.exists()) {
            logger.info("导出目录不存在，尝试创建");
            try {
                if (savePath.mkdirs()) {
                    logger.info("创建导出目录成功，路径：{}",savePath.getAbsolutePath());
                }
            } catch (SecurityException securityException) {
                logger.error("尝试创建目录失败，请检查目录权限或输入路径是否正确，路径：{}",savePath.getAbsolutePath());
            }
        }
    }

    /**
     * 保存消息到文件
     *
     * @param data 单条数据
     * @return 是否成功
     */
    public Boolean saveMessageToFile(DanMuData data) {
        //TODO 暂时处理逻辑：检查当前是否有记录文件，无则按指定格式创建，
        // 有则检查分隔时间，超过时新建文件并写入，否则直接写入。
        // 在检测到文件无法写入时，暂时保存到缓冲区（限制数量）或尝试新建文件，输出日志
        return null;
    }

    /**
     * 单弹幕导出操作
     *
     * @param danMuData 弹幕信息
     * @return 是否导出成功
     */
    @Override
    public Boolean export(DanMuData danMuData) {
        logger.debug(danMuData.toString());
        return saveMessageToFile(danMuData);
    }
}
