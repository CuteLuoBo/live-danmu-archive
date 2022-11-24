package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuExportDataInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author CuteLuoBo
 * @date 2022/11/24 16:12
 */
public abstract class Sqlite2FileDanMuFormatExportService extends SqliteDanMuFormatExportService<DanMuExportDataInfo<File>> {

    /**
     * 直播名称，用于文件命名
     */
    private String liveName;

    /**
     * 存档文件路径
     */
    private File saveFilePath;

    private final String saveFileSuffix;

    public Sqlite2FileDanMuFormatExportService(@NotNull List<File> sqliteFileList, String liveName, File saveFilePath, String saveFileSuffix) throws ServiceException {
        super(sqliteFileList);
        this.liveName = liveName;
        if (!saveFilePath.exists() || !saveFilePath.isDirectory()) {
            throw new ServiceException("传入的文件保存路径无效");
        }
        this.saveFileSuffix = saveFileSuffix;
        this.saveFilePath = saveFilePath;
    }

    /**
     * 根据时间范围创建文件名称
     *
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @return 文件名称
     */
    private String createSaveFileNameByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        StringBuilder saveFileName = new StringBuilder();
        saveFileName.append(saveFilePath.getAbsolutePath()).append(File.separator)
                .append(liveName).append("-R-");
        //以记录包含时间命名
        saveFileName.append("(")
                .append(startTime.format(DateTimeFormatter.ofPattern(fileNameTimeFormat)))
                .append("~");
        if (endTime != null) {
            saveFileName.append(endTime.format(DateTimeFormatter.ofPattern(fileNameTimeFormat)));
        } else {
            saveFileName.append("now");
        }
        saveFileName.append(")");
        return saveFileName.toString();
    }

    /**
     * 用于初始创建文件并写入
     *
     * @param saveFile 将保存的文件对象
     * @return 创建完成的文件
     * @throws IOException 创建或写入时出现IO错误
     */
    protected File createFileAndWriteHead(File saveFile) throws IOException{
        //默认创建空文件
        Files.writeString(saveFile.toPath(),
                "",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return saveFile;
    }

    /**
     * 读取数据库文件，读取并添加弹幕信息到文件中
     *
     * @param videoStartTimeStamp    视频开始的时间戳，用于弹幕时间轴匹配
     * @param saveFile               保存的文件对象
     * @param danMuDataModelSelector 弹幕数据的选择类，用于数据库筛选
     * @return 组装好的导出数据信息
     * @throws IOException 当写入文件时出现问题
     */
    protected abstract DanMuExportDataInfo<File> writeDanMuData(long videoStartTimeStamp, File saveFile, DanMuDataModelSelector danMuDataModelSelector) throws IOException;

        /**
         * 导出指定筛选的弹幕结果
         *
         * @param startTime 开始时间
         * @param endTime   结束时间,为null时为不限
         * @return 导出的指定包装对象
         * @throws IOException IO错误
         */
    @Override
    public DanMuExportDataInfo<File> formatExportBySelector(LocalDateTime startTime, LocalDateTime endTime) throws IOException {
        //转换时区用
        OffsetDateTime offsetDateTime = OffsetDateTime.now();

        //创建保存文件对象
        File saveFile = new File(createSaveFileNameByTimeRange(startTime, endTime) + saveFileSuffix);
        //创建文件与写入头部信息
        createFileAndWriteHead(saveFile);

        //数据库筛选对象
        DanMuDataModelSelector danMuDataModelSelector = new DanMuDataModelSelector();
        danMuDataModelSelector.setStartCreateTime(startTime.toInstant(offsetDateTime.getOffset()).toEpochMilli());
        if (endTime != null) {
            danMuDataModelSelector.setEndCreateTime(endTime.toInstant(offsetDateTime.getOffset()).toEpochMilli());
        }

        //匹配起始时间戳
        long videoStartTimeStamp = startTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();

        //读取并添加弹幕信息返回
        return writeDanMuData(videoStartTimeStamp,saveFile,danMuDataModelSelector);
    }
}
