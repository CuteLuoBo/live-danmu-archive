package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuFormatModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuUserInfoModel;
import com.github.cuteluobo.livedanmuarchive.pojo.*;
import com.github.cuteluobo.livedanmuarchive.utils.FormatUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SQLite数据转ASS文件服务实现类
 * 应该做更高解耦，但拆分时重新分装对象会导致额外性能损耗
 * @author CuteLuoBo
 * @date 2022/11/13 11:17
 */
public class Sqlite2AssFileDanMuFormatExportServiceImpl extends Sqlite2FileDanMuFormatExportService {

    public static String SAVE_FILE_SUFFIX = ".ass";

    public Sqlite2AssFileDanMuFormatExportServiceImpl(@NotNull List<File> sqliteFileList, String liveName, File saveFilePath) throws ServiceException {
        super(sqliteFileList, liveName, saveFilePath, SAVE_FILE_SUFFIX);
    }

    /**
     * 按传入数据构建样式列表
     * @return 组装好的ass style字符串
     */
    private String createStylesString() {
        Map<String, DanMuFormatModel> danMuFormatIndexMap = getDanMuFormatIndexMap();
        StringBuilder sb = new StringBuilder();
        if (danMuFormatIndexMap != null) {
            /*
             * ass中字幕位置的定位基准(小键盘布局)
             * _______
             * |1 2 3|
             * |4 5 6|
             * |7 8 9|
             * -------
             * */
            int locationStandard = 7;
            sb.append("[V4+ Styles]\r\n")
                    .append("Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding\r\n")
                    .append("Style: ").append(NORMAL_STYLE_NAME).append(",").append(NORMAL_STYLE_FONT).append(",25,&Hffffff,&H&Hffffff,&H00000000,&H00000000,0,0,0,0,100,100,0,0,1,2,3,").append(locationStandard).append(",20,20,20,1\r\n")
            ;
            //16进制格式约束
    //        DecimalFormat hex = new DecimalFormat("00000000");
            for (Map.Entry<String, DanMuFormatModel> entry :
                    danMuFormatIndexMap.entrySet()) {
                DanMuFormat dfm = entry.getValue();
                sb.append("Style: ")
                        //代号
                        .append(entry.getKey()).append(",")
                        //字体
                        .append(NORMAL_STYLE_FONT).append(",")
                        //字体大小
                        .append(Math.min(getMaxFontSize(), Math.max(getMixFontSize(), dfm.getFontSize()))).append(",")
                        //字体颜色
                        .append("&H").append(Integer.toHexString(dfm.getFontColor())).append(",")
                        .append("&H").append(Integer.toHexString(dfm.getFontColor())).append(",")
                        //描边颜色
                        .append("&H00000000").append(",")
                        //阴影颜色
                        .append("&H00000000").append(",")
                        //其他剩余样式
                        .append("0,0,0,0,100,100,0,0,1,2,0,").append(locationStandard).append(",20,20,2,1")
                        .append("\r\n");

            }
        }
        return sb.toString();
    }

    /**
     * 打印ASS字幕基础信息
     */
    private String createAssInfo(){
        //组装信息
        String sb = "[Script Info]\r\n" +
                "Title:LiveDanMuExport\r\n" +
                "Original Script:" + "live-danmu-archive" + " on " + LocalDateTime.now() + "\r\n" +
                "ScriptType:v4.00+\r\n" +
                "Collisions:Normal\r\n" +
                "PlayResX:" + getVideoWidth() + "\r\n" +
                "PlayResY:" + getVideoHeight() + "\r\n" +
                "Timer:100.0000\r\n" +
                "WrapStyle: 0\r\n" +
                "ScaledBorderAndShadow: no\r\n" +
                "\r\n";
        return sb;
    }

    /**
     * 创建事件头
     * @return 事件头字符串
     */
    private String createEventHead() {
        return "[Events]\r\n" + "Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\r\n";
    }

    /**
     * 创建文件并写入
     * @param saveFile 将保存的文件对象
     * @return 创建完成的文件
     * @throws IOException 创建或写入时出现IO错误
     */
    @Override
    protected File createFileAndWriteHead(File saveFile) throws IOException {
        //写入头部
        Files.writeString(saveFile.toPath(),
                createAssInfo() + createStylesString() + createEventHead(),
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
    @Override
    protected String conventDanMuData(long videoStartTimeStamp, FormatDanMuData[] trackTempArray, int sqlDataIndex, List<DanMuDataModel> danMuDataModelList, float trackHeight, int trackNum) {
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
                //TODO 增加其他样式弹出的弹幕样式支持

                //允许弹幕间间隔
                int allowSpace = getMaxFontSize();
                //检查是否显示用户名
                if (isShowSenderName()) {
                    for (int k = 0; k < trackNum; k++) {
                        //获取当前轨道的上一个弹幕
                        FormatDanMuData before = trackTempArray[k];
                        //检查弹幕是否冲突
                        boolean notOver = true;
                        if (before != null) {

                            notOver = !checkDanMuOverlap(getVideoWidth(), before, danMuData, allowSpace, getShowTime());
                        }
                        if (notOver && danMuData.getContent()!=null) {
                            //更新占位缓存
                            trackTempArray[k] = danMuData;
                            //用户数据
                            DanMuUserInfoModel danMuUserInfoModel = getDanMuUserInfoTempByIndexAndId(sqlDataIndex, d.getUserId());
                            return createAssDialogue(danMuStartTime, getShowTime()
                                    , danMuFormat == null ? NORMAL_STYLE_NAME : sqlDataIndex + "-" + d.getFormat()
                                    , k * trackHeight * 1.2f
                                    ,(danMuUserInfoModel==null?"":danMuUserInfoModel.getNickName()+":")+danMuData.getContent()
                                    ,danMuData.getFontSize()
                            );
                        }
                    }
                } else {
                    for (int k = 0; k < trackNum; k++) {
                        //获取当前轨道的上一个弹幕
                        FormatDanMuData before = trackTempArray[k];
                        //检查弹幕是否冲突
                        boolean notOver = true;
                        if (before != null) {
                            notOver = !checkDanMuOverlap(getVideoWidth(), before, danMuData, allowSpace, getShowTime());
                        }
                        if (notOver && danMuData.getContent()!=null) {
                            //更新占位缓存
                            trackTempArray[k] = danMuData;
                            return createAssDialogue(danMuStartTime, getShowTime()
                                    , danMuFormat == null ? NORMAL_STYLE_NAME : sqlDataIndex + "-" + d.getFormat()
                                    , k * trackHeight * 1.2f
                                    ,danMuData.getContent()
                                    ,danMuData.getFontSize()
                            );
                        }
                    }
                }
            }
        }
        return "";
    }

    /**
     * 创建ASS普通弹幕字幕
     *
     * @param danMuStartTime 弹幕起始时间
     * @param showTime       显示时间
     * @param styleName      使用样式名称
     * @param trackY    轨道Y坐标
     * @param content        内容
     * @param fontSize       字体大小
     * @return 组装完成的字符串
     */
    private String createAssDialogue(long danMuStartTime, long showTime, String styleName, float trackY, String content, float fontSize) {
        return "Dialogue:0," +
                //时间
                FormatUtil.millTime2String(danMuStartTime) + "," +
                FormatUtil.millTime2String(danMuStartTime + showTime) + "," +
                //样式
                styleName + "," +
                ",0000,0000,0000,," +
                /*
                 * \move(<x1>, <y1>, <x2>, <y2>[, <t1>, <t2>])
                 * ①x1,y1为移动开始的位置，x2,y2为移动结束的位置；参照点(移动点)由对齐方式决定
                 * ②t1,t2是移动开始和结束时间，单位 [ms]，缺省则在此字幕持续时间内进行移动
                 * ③所有变量均可用小数
                 * */
                "{\\move(" +getVideoWidth() + "," + trackY + "," + -content.length() * fontSize + "," + trackY +")}" +
                //内容
                content +
                "\r\n";
    }
}
