package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuDataModelMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuUserInfoModelMapper;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuUserInfoModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuExportDataInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.DataPage;
import com.github.cuteluobo.livedanmuarchive.pojo.FormatDanMuData;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
     * 转换弹幕消息为字符串
     * @param videoStartTimeStamp  视频起始时间戳（作为基准）
     * @param trackTempArray      轨道缓存数组（用于对比）
     * @param sqlDataIndex         sql数据的索引
     * @param danMuDataModelList   弹幕数据列表
     * @param trackHeight          单轨道高度
     * @param trackNum             允许的轨道数量
     * @return 字符串
     */
    protected abstract String conventDanMuData(long videoStartTimeStamp, FormatDanMuData[] trackTempArray, int sqlDataIndex, List<DanMuDataModel> danMuDataModelList, float trackHeight, int trackNum);


        /**
         * 读取数据库文件，读取并添加弹幕信息到文件中
         *
         * @param videoStartTimeStamp    视频开始的时间戳，用于弹幕时间轴匹配
         * @param saveFile               保存的文件对象
         * @param danMuDataModelSelector 弹幕数据的选择类，用于数据库筛选
         * @return 组装好的导出数据信息
         * @throws IOException 当写入文件时出现问题
         */
    protected DanMuExportDataInfo<File> writeDanMuData(long videoStartTimeStamp, File saveFile, DanMuDataModelSelector danMuDataModelSelector) throws IOException {
        DanMuExportDataInfo<File> danMuExportDataInfo = new DanMuExportDataInfo<>();
        danMuExportDataInfo.setData(saveFile);
        //允许的轨道数量
        float trackHeight = getMaxFontSize() * 1.2f;
        int trackNum = (int) ((getVideoHeight() * getScreenProp()) / trackHeight);
        //用于对比是否覆盖的缓存数组，上限为轨道数
        FormatDanMuData[] beforeTempArray = new FormatDanMuData[trackNum];

        //数据统计信息
        long totalDanMuNum = 0;
        long usageDanMuNum = 0;

        List<SqlSessionFactory> sqlSessionFactories = getSqliteFileSessionFactoryList();
        if (sqlSessionFactories != null) {
            StringBuilder tempStringBuilder = new StringBuilder();
            //单次分页取出数量
            int pageSize = 1000;
            //对于多个数据源
            for (int i = 0; i < sqlSessionFactories.size(); i++) {
                SqlSessionFactory sf = sqlSessionFactories.get(i);
                try(SqlSession sqlSession = sf.openSession()){
                    DanMuDataModelMapper mapper = sqlSession.getMapper(DanMuDataModelMapper.class);
                    DanMuUserInfoModelMapper danMuUserInfoModelMapper= sqlSession.getMapper(DanMuUserInfoModelMapper.class);
                    //尝试获取第一页，检查数据
                    DataPage<DanMuDataModel> page = mapper.listPage(danMuDataModelSelector, 0, pageSize);
                    //后续增加关键词过滤时，具体使用的数量可能需要调整（数据库过滤或程序过滤）
                    usageDanMuNum += page.getTotal();

                    if (page.getTotal() > 0) {
                        //累计查询弹幕数量
                        totalDanMuNum += page.getTotal();

                        int maxPage = page.getMaxPageNum();
                        for (int j = 0; j <= maxPage; j++) {
                            //显示用户名时，对用户数据进行缓存
                            if (isShowSenderName()) {
                                Set<Integer> ids = page.getData().stream().map(DanMuDataModel::getUserId).collect(Collectors.toSet());
                                List<DanMuUserInfoModel> danMuUserInfoModelList = danMuUserInfoModelMapper.getListById(ids);
                                appendDanMuUserInfoTemp(i, danMuUserInfoModelList);
                            }
                            //转换弹幕数据
                            tempStringBuilder.append(conventDanMuData(videoStartTimeStamp, beforeTempArray, i, page.getData(), trackHeight, trackNum));
                            //写入并清空缓存
                            Files.writeString(saveFile.toPath(),
                                    tempStringBuilder.toString(),
                                    StandardOpenOption.APPEND);
                            tempStringBuilder.delete(0, tempStringBuilder.length());
                            //从数据库中读取更新数据
                            page = mapper.listPage(danMuDataModelSelector, j + 1, pageSize);
                        }
                    }
                }
            }
        }
        //设置统计数据
        danMuExportDataInfo.setTotalNum(totalDanMuNum);
        danMuExportDataInfo.setUsageNum(usageDanMuNum);
        return danMuExportDataInfo;
    }

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
