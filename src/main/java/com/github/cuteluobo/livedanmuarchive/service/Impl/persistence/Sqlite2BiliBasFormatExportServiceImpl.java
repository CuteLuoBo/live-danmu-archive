package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuDataModelMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuFormatModelMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuUserInfoModelMapper;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuFormatModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuUserInfoModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuExportDataInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuFormat;
import com.github.cuteluobo.livedanmuarchive.pojo.DataPage;
import com.github.cuteluobo.livedanmuarchive.pojo.FormatDanMuData;
import com.github.cuteluobo.livedanmuarchive.service.DanMuFormatExportService;
import com.github.cuteluobo.livedanmuarchive.utils.DatabaseConfigUtil;
import com.github.cuteluobo.livedanmuarchive.utils.FormatUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据库弹幕文件转B站BAS弹幕样式
 * https://bilibili.github.io/bas/#/guide
 * @author CuteLuoBo
 * @date 2022/11/22 15:58
 */
public class Sqlite2BiliBasFormatExportServiceImpl extends Sqlite2FileDanMuFormatExportService {

    /**
     * 默认定义的轨道前缀(前缀+轨道数字)
     */
    private final String NORMAL_TRACK_STYLE_PREFIX = "L";

    public Sqlite2BiliBasFormatExportServiceImpl(@NotNull List<File> sqliteFileList, String liveName, File saveFilePath, String saveFileSuffix) throws ServiceException {
        super(sqliteFileList, liveName, saveFilePath, saveFileSuffix);
    }


    private String conventDanMuData(long videoStartTimeStamp, FormatDanMuData[] beforeTempArray, int sqlDataIndex, List<DanMuDataModel> danMuDataModelList, float trackHeight, int trackNum) {
        StringBuilder sb = new StringBuilder();
        if (danMuDataModelList != null && !danMuDataModelList.isEmpty()) {
            for (DanMuDataModel d : danMuDataModelList
            ) {
                //此弹幕出现的起始时间
                long danMuStartTime = d.getCreateTime() - videoStartTimeStamp;
                //从缓存map中读取弹幕样式(sqlite文件索引-当前sqlite文件的弹幕格式ID)
                DanMuFormat danMuFormat = getDanMuFormatIndexMap().get(sqlDataIndex + "-" + d.getFormat());
                //缓存用弹幕数据
                FormatDanMuData danMuData = new FormatDanMuData(danMuStartTime, d.getData()
                        , danMuFormat == null ? NORMAL_FONT_SIZE : Math.max(getMixFontSize(), danMuFormat.getFontSize()));
                //TODO 后续增加其他样式弹出(居中/底部)的弹幕样式支持
                //对象分隔字符，用于节约缩进符占用
//                String lineSeparator = "\r\n";
//                String indentSeparator = "\t";
                String lineSeparator = " ";
                String indentSeparator = "";
                //外部判断是否展示用户名，避免内部多次判断影响性能
                if (isShowSenderName()) {
                    for (int k = 0; k < trackNum; k++) {
                        FormatDanMuData before = beforeTempArray[k];
                        //检查弹幕是否冲突
                        boolean notOver = true;
                        if (before != null) {
                            int allowSpace = getMaxFontSize() * 2;
                            notOver = !checkDanMuOverlap(getVideoWidth(), before, danMuData, allowSpace, getShowTime());
                        }
                        if (notOver && danMuData.getContent()!=null) {
                            //更新占位缓存
                            beforeTempArray[k] = danMuData;
                            //初始化bas的对象名称
                            String styleName = NORMAL_TRACK_STYLE_PREFIX + k +"i"+ d.getId();
                            //预计的弹幕宽度
                            float contentWidth = danMuData.getContent().length() * danMuData.getFontSize();
                            DanMuUserInfoModel danMuUserInfoModel = getDanMuUserInfoTempByIndexAndId(sqlDataIndex, d.getUserId());

                            //A.每个弹幕都用独立的对象。后续可换成可复用对象以进行优化BAS性能
                            sb.append("def text ").append(styleName).append(" {").append(lineSeparator)
                                    .append(indentSeparator).append("content = \"").append(danMuUserInfoModel==null?"":danMuUserInfoModel.getNickName()+":").append(FormatUtil.replaceSymbol(danMuData.getContent())).append("\"").append(lineSeparator)
                                    .append(indentSeparator).append("fontSize = ").append(danMuData.getFontSize()).append(lineSeparator);
                            //字体颜色
                            if (danMuFormat != null) {
                                sb.append(indentSeparator).append(createColorString(danMuFormat.getFontColor())).append(lineSeparator);
                            }
                            //延迟显示代码
                            sb.append(createShowCode(indentSeparator, lineSeparator, k * trackHeight * 1.2f, styleName, danMuStartTime, contentWidth));
                            break;
                        }
                    }
                } else {
                    //循环查找并分配轨道，轨道满载时当前弹幕抛弃
                    for (int k = 0; k < trackNum; k++) {
                        FormatDanMuData before = beforeTempArray[k];
                        //检查弹幕是否冲突
                        boolean notOver = true;
                        if (before != null) {
                            int allowSpace = getMaxFontSize() * 2;
                            notOver = !checkDanMuOverlap(getVideoWidth(), before, danMuData, allowSpace, getShowTime());
                        }
                        if (notOver && danMuData.getContent()!=null) {
                            //更新占位缓存
                            beforeTempArray[k] = danMuData;
                            //初始化bas的对象名称
                            String styleName = NORMAL_TRACK_STYLE_PREFIX + k +"i"+ d.getId();
                            //预计的弹幕宽度
                            float contentWidth = danMuData.getContent().length() * danMuData.getFontSize();
                            //A.每个弹幕都用独立的对象。后续可换成可复用对象以进行优化BAS性能
                            sb.append("def text ").append(styleName).append(" {").append(lineSeparator)
                                    .append(indentSeparator).append("content = \"").append(FormatUtil.replaceSymbol(danMuData.getContent())).append("\"").append(lineSeparator)
                                    .append(indentSeparator).append("fontSize = ").append(danMuData.getFontSize()).append(lineSeparator);
                            //字体颜色
                            if (danMuFormat != null) {
                                sb.append(indentSeparator).append(createColorString(danMuFormat.getFontColor())).append(lineSeparator);
                            }
                            //延迟显示代码
                            sb.append(createShowCode(indentSeparator, lineSeparator, k * trackHeight * 1.2f, styleName, danMuStartTime, contentWidth));
                            break;
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    private String createColorString(int color) {
        return "color = 0x" + Integer.toHexString(color);
    }

    /**
     * 创建延迟显示代码
     * @param indentSeparator 前置分隔符
     * @param lineSeparator   换行符
     * @param y               y坐标
     * @param styleName       样式对象名称
     * @param danMuStartTime  弹幕起始显示时间
     * @param contentWidth    内容宽度
     * @return 代码文本
     */
    private String createShowCode(String indentSeparator, String lineSeparator, float y, String styleName, long danMuStartTime, float contentWidth) {
        //暂时隐藏
        String sb = indentSeparator + "x = 200%" + lineSeparator +
                indentSeparator + "y = " + y + lineSeparator +
                "}" + lineSeparator +
                //定时出现
                "set " + styleName + "{" + lineSeparator +
                indentSeparator + "x = 100%" + lineSeparator +
                "} " + danMuStartTime + "ms" + lineSeparator +
                //实际展示
                "then set " + styleName + "{" + lineSeparator +
                indentSeparator + "x = -" + contentWidth +
                indentSeparator + "duration = " + getShowTime() + "ms" + lineSeparator +
                "} " + getShowTime() + "ms" + lineSeparator;
        return sb;
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
    @Override
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
}
