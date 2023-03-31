package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuUserInfoModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuFormat;
import com.github.cuteluobo.livedanmuarchive.pojo.FormatDanMuData;
import com.github.cuteluobo.livedanmuarchive.utils.FormatUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

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

    /**
     * 默认颜色
     */
    private final String NORMAL_COLOR = "0xFFFFFF";

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
                boolean lowIndent = false;
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
                            //用户数据
                            DanMuUserInfoModel danMuUserInfoModel = getDanMuUserInfoTempByIndexAndId(sqlDataIndex, d.getUserId());
                            //具体内容，根据缓存显示用户名称
                            String content = (danMuUserInfoModel == null ? "" : danMuUserInfoModel.getNickName() + ":") + FormatUtil.replaceSymbol(danMuData.getContent());
                            //预计的弹幕宽度
                            float contentWidth = content.length() * danMuData.getFontSize();
                            //复用
                            sb.append(createReuseBasObject(lowIndent, before == null
                                    , k, trackHeight, d.getId()
                                    , content
                                    , String.valueOf(danMuData.getFontSize()), danMuFormat
                                    , danMuStartTime
                                    , before == null ? 0 : danMuStartTime - before.getStartTime()
                                    , contentWidth));
//                            //初始化bas的对象名称
//                            String styleName = NORMAL_TRACK_STYLE_PREFIX + k +"i"+ d.getId();
//                            sb.append(
//                                    createSingleBasObject(lowIndent, styleName
//                                            , content
//                                            , String.valueOf(danMuData.getFontSize())
//                                            , danMuFormat
//                                            , k * trackHeight * 1.2f
//                                            , danMuStartTime
//                                            , contentWidth)
//                            );
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
                            //预计的弹幕宽度
                            float contentWidth = danMuData.getContent().length() * danMuData.getFontSize();
                            //复用
                            sb.append(createReuseBasObject(lowIndent, before == null
                                    , k, trackHeight, d.getId()
                                    , FormatUtil.replaceSymbol(danMuData.getContent())
                                    , String.valueOf(danMuData.getFontSize()), danMuFormat
                                    , danMuStartTime
                                    , before == null ? 0 : danMuStartTime - before.getStartTime()
                                    , contentWidth));
                            //初始化bas的对象名称
//                            String styleName = NORMAL_TRACK_STYLE_PREFIX + k +"i"+ d.getId();
//                            sb.append(
//                                    createSingleBasObject(lowIndent, styleName
//                                    , FormatUtil.replaceSymbol(danMuData.getContent())
//                                    , String.valueOf(danMuData.getFontSize())
//                                    , danMuFormat
//                                    , k * trackHeight * 1.2f
//                                    , danMuStartTime
//                                    , contentWidth)
//                            );
                            break;
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * 用于初始创建文件并写入
     *
     * @param saveFile 将保存的文件对象
     * @return 创建完成的文件
     * @throws IOException 创建或写入时出现IO错误
     */
    @Override
    protected File createFileAndWriteHead(File saveFile) throws IOException {
        //默认创建空文件
        Files.writeString(saveFile.toPath(),
                "",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return saveFile;
    }

    private String createTrackObjectName(int track) {
        return NORMAL_TRACK_STYLE_PREFIX + track;
    }

    /**
     * 创建复用的弹幕BAS对象
     * @param lowIndent     省略缩进
     * @param isNew          是否为新对象
     * @param track          当前的轨道数
     * @param trackHeight    轨道高度
     * @param content        内容正文
     * @param fontSizeString 字体大小字符串
     * @param danMuFormat    弹幕样式
     * @param intervalTime   与之前弹幕的间隔时间
     * @param contentWidth   内容宽度
     * @return  构建完成的BAS对象字符串
     */
    private String createReuseBasObject(boolean lowIndent,boolean isNew,int track,float trackHeight,long id,String content,String fontSizeString,DanMuFormat danMuFormat,long danmuStartTime, long intervalTime, float contentWidth) {
        String lineSeparator = "\r\n";
        String indentSeparator = "\t";
        if (lowIndent) {
            lineSeparator = " ";
            indentSeparator = "";
        }
        StringBuilder sb = new StringBuilder();
        //初始轨道对象(定义轨道Y坐标)
        if (isNew) {
            sb.append("def text ").append(createTrackObjectName(track)).append("(newX = 100% ,newFontSize = 25,newColor = ").append(NORMAL_COLOR).append(") { ").append(lineSeparator)
                    .append(indentSeparator).append("content = \"\"").append(lineSeparator)
                    .append(indentSeparator).append("y = ").append(track * trackHeight * 1.2f).append(lineSeparator)
                    .append(indentSeparator).append("x = newX").append(lineSeparator)
                    .append(indentSeparator).append("fontSize = newFontSize").append(lineSeparator)
                    .append(indentSeparator).append("color = newColor").append(lineSeparator)
                    .append("}").append(lineSeparator)
            ;
        }
        String objectName = createObjectName(id);
        //变更信息
        sb.append("let ").append(objectName).append(" = ").append(createTrackObjectName(track)).append(" (").append(lineSeparator)
                .append(indentSeparator)
                .append("100%").append(",")
                .append(fontSizeString).append(",")
                .append(danMuFormat == null ? NORMAL_COLOR : createColorString(danMuFormat.getFontColor()))
                .append(")").append(lineSeparator)
        ;
        //串行设置定时间隔
        sb.append("set ").append(objectName).append("{").append(lineSeparator)
                .append(indentSeparator).append("content = \"").append(content).append("\"").append(lineSeparator)
                .append(indentSeparator).append("x = 100%").append(lineSeparator)
                .append("} ").append(danmuStartTime).append("ms").append(lineSeparator)
        ;
        //实际显示
        sb.append("then set ").append(objectName).append("{").append(lineSeparator)
                .append(indentSeparator).append("x = -").append(contentWidth).append(lineSeparator)
                .append("} ").append(getShowTime()).append("ms").append(lineSeparator)
        ;
        return sb.toString();
    }

    private String createObjectName(long id) {
        return "o" + id;
    }

    /**
     * 创建一个模拟弹幕的BAS单例对象
     * @param lowIndent      省略缩进
     * @param styleName      样式（对象）名称
     * @param content        显示内容
     * @param fontSizeString 字体尺寸String字符串(小数或百分比)
     * @param danMuFormat    弹幕样式
     * @param y              弹幕所在Y坐标（用于轨道区分）
     * @param danMuStartTime 弹幕对于视频的起始时间
     * @param contentWidth   内容字体宽度
     * @return 组装完成的字符串
     */
    private String createSingleBasObject(boolean lowIndent,String styleName,String content,String fontSizeString,DanMuFormat danMuFormat, float y, long danMuStartTime, float contentWidth) {
        String lineSeparator = "\r\n";
        String indentSeparator = "\t";
        if (lowIndent) {
            lineSeparator = " ";
            indentSeparator = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("def text ").append(styleName).append(" {").append(lineSeparator)
                .append(indentSeparator).append("content = \"").append(content).append("\"").append(lineSeparator)
                .append(indentSeparator).append("fontSize = ").append(fontSizeString).append(lineSeparator);
        //字体颜色
        if (danMuFormat != null) {
            sb.append(indentSeparator).append("color = ").append(createColorString(danMuFormat.getFontColor())).append(lineSeparator);
        }
        sb.append(indentSeparator).append("x = 200%").append(lineSeparator)
                .append(indentSeparator).append("y = ").append(y).append(lineSeparator)
                .append("}").append(lineSeparator)
                .append("set ").append(styleName)
                .append("{").append(lineSeparator)
                .append(indentSeparator).append("x = 100%").append(lineSeparator)
                .append("} ").append(danMuStartTime).append("ms").append(lineSeparator)
                .append("then set ").append(styleName).append("{").append(lineSeparator)
                .append(indentSeparator).append("x = -").append(contentWidth).append(lineSeparator)
                .append(indentSeparator).append("duration = ").append(getShowTime()).append("ms")
                .append(lineSeparator).append("} ").append(getShowTime()).append("ms").append(lineSeparator);
        return sb.toString();
    }

    private String createColorString(int color) {
        return "0x" + Integer.toHexString(color);
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
                        indentSeparator + "x = -" + contentWidth + lineSeparator +
                        indentSeparator + "duration = " + getShowTime() + "ms" + lineSeparator +
                        "} " + getShowTime() + "ms" + lineSeparator;
        return sb;
    }
}
