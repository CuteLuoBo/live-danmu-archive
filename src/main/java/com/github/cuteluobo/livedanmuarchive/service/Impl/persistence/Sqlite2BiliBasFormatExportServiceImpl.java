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
    @Override
    protected String conventDanMuData(long videoStartTimeStamp, FormatDanMuData[] trackTempArray, int sqlDataIndex, List<DanMuDataModel> danMuDataModelList, float trackHeight, int trackNum) {
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
                        FormatDanMuData before = trackTempArray[k];
                        //检查弹幕是否冲突
                        boolean notOver = true;
                        if (before != null) {
                            int allowSpace = getMaxFontSize() * 2;
                            notOver = !checkDanMuOverlap(getVideoWidth(), before, danMuData, allowSpace, getShowTime());
                        }
                        if (notOver && danMuData.getContent()!=null) {
                            //更新占位缓存
                            trackTempArray[k] = danMuData;
                            //初始化bas的对象名称
                            String styleName = NORMAL_TRACK_STYLE_PREFIX + k +"i"+ d.getId();
                            //用户数据
                            DanMuUserInfoModel danMuUserInfoModel = getDanMuUserInfoTempByIndexAndId(sqlDataIndex, d.getUserId());
                            //具体内容，根据缓存显示用户名称
                            String content = (danMuUserInfoModel == null ? "" : danMuUserInfoModel.getNickName() + ":") + FormatUtil.replaceSymbol(danMuData.getContent());
                            //预计的弹幕宽度
                            float contentWidth = content.length() * danMuData.getFontSize();


                            //A.每个弹幕都用独立的对象。后续可换成可复用对象以进行优化BAS性能
                            sb.append("def text ").append(styleName).append(" {").append(lineSeparator)
                                    .append(indentSeparator).append("content = \"").append(content).append("\"").append(lineSeparator)
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
                        FormatDanMuData before = trackTempArray[k];
                        //检查弹幕是否冲突
                        boolean notOver = true;
                        if (before != null) {
                            int allowSpace = getMaxFontSize() * 2;
                            notOver = !checkDanMuOverlap(getVideoWidth(), before, danMuData, allowSpace, getShowTime());
                        }
                        if (notOver && danMuData.getContent()!=null) {
                            //更新占位缓存
                            trackTempArray[k] = danMuData;
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

        String sb =
                //暂时隐藏
                indentSeparator + "x = 200%" + lineSeparator +
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
}
