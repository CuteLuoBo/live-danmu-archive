package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuDataModelMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuFormatModelMapper;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuFormatModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuExportDataInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuFormat;
import com.github.cuteluobo.livedanmuarchive.pojo.DataPage;
import com.github.cuteluobo.livedanmuarchive.service.DanMuFormatExportService;
import com.github.cuteluobo.livedanmuarchive.utils.DatabaseConfigUtil;
import com.github.cuteluobo.livedanmuarchive.utils.FormatUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * SQLite数据转ASS文件服务实现类
 * 应该做更高解耦，但拆分时重新分装对象会导致额外性能损耗
 * @author CuteLuoBo
 * @date 2022/11/13 11:17
 */
public class Sqlite2AssFileDanMuFormatExportServiceImpl implements DanMuFormatExportService<DanMuExportDataInfo<File>> {
    /**
     * SQLITE文件列表
     */
    private List<File> sqliteFileList;

    /**
     * 由文件生成的对应访问工厂
     */
    private List<SqlSessionFactory> sqliteFileSessionFactoryList;


    /**
     * 允许的最大字体大小,超过的将调整为此
     */
    private int maxFontSize = 30;
    /**
     * 最小的字体大小，防止显示效果不佳
     */
    private int mixFontSize = 18;

    /**
     * 显示弹幕发送者名称
     */
    private boolean showSenderName = false;

    /**
     * 视频宽度
     */
    private int videoWidth = 1920;

    /**
     * 视频高度
     */
    private int videoHeight = 1080;

    /**
     * 弹幕中使用的默认字体
     */
    private final String NORMAL_STYLE_FONT = "黑体";

    /**
     * 默认样式的名称
     */
    private final String NORMAL_STYLE_NAME = "normal";

    /**
     * 默认字体大小
     */
    private final float NORMAL_FONT_SIZE = 25.0f;

    /**
     * 文件时间命名格式
     */
    private String fileNameTimeFormat = "yyyy-MM-dd HH-mm-ss";

    /**
     * 直播名称，用于文件命名
     */
    private String liveName;
    /**
     * 弹幕占屏幕比例
     */
    private float screenProp = 0.6f;

    /**
     * 弹幕显示时间(ms)
     */
    private int showTime = 8000;

    /**
     * 弹幕样式的命名和具体对象索引
     * 命名格式：<数据源索引>-<当前样式在此数据源中的排序>
     */
    private Map<String, DanMuFormatModel> danMuFormatIndexMap = new HashMap<>(32);

    private File saveAssFilePath;

    public Sqlite2AssFileDanMuFormatExportServiceImpl(String liveName, List<File> saveSqliteFileList, File saveAssFilePath) throws ServiceException {
        this.liveName = liveName;
        this.sqliteFileList = saveSqliteFileList;
        //生成对应访问工厂
        sqliteFileSessionFactoryList = new ArrayList<>(saveSqliteFileList.size());
        if (saveAssFilePath == null || !saveAssFilePath.exists() || !saveAssFilePath.isDirectory()) {
            throw new ServiceException("传入的文件保存路径无效");
        }
        this.saveAssFilePath = saveAssFilePath;
        if (saveSqliteFileList.isEmpty()) {
            throw new ServiceException("传入的Sqlite列表不能为空");
        }
        for (File f :
                saveSqliteFileList) {
            if (!f.exists()) {
                throw new ServiceException("传入的Sqlite文件不存在");
            }
            sqliteFileSessionFactoryList.add(DatabaseConfigUtil.initFileDatabaseConnectFactory(f));
        }
    }

    /**
     * 按传入数据构建样式列表
     * @param danMuFormatIndexMap 弹幕样式索引map
     * @return 组装好的ass style字符串
     */
    private String createStylesString(Map<String, DanMuFormatModel> danMuFormatIndexMap) {
        StringBuilder sb = new StringBuilder();
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
                    .append(Math.min(maxFontSize, Math.max(mixFontSize, dfm.getFontSize()))).append(",")
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
                "PlayResX:" + videoWidth + "\r\n" +
                "PlayResY:" + videoHeight + "\r\n" +
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
     * 判断弹幕是否会重叠
     *
     * @param screenWidth  屏幕宽度
     * @param before       之前的弹幕
     * @param now          当前计算弹幕
     * @param showTime     弹幕展现时间(ms)
     * @return 判断结果
     */
    public static boolean checkDanMuOverlap(int screenWidth, Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData before, Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData now, int showTime) {
        long intervalTime = now.getStartTime() - before.getStartTime();
        if (intervalTime > showTime) {
            return false;
        }
        //弹幕间距
        int space = (int) (before.getFontSize() / 2);
        float beforeWidth = before.getContent().length() * before.getFontSize();
        float beforeSpeed = (screenWidth + beforeWidth) / showTime;
        //之前弹幕的尾部坐标
        int beforeLastPoint = (int) (screenWidth + beforeWidth - (beforeSpeed * intervalTime) );
        //之前弹幕仍未完全显示完成
        if (beforeLastPoint + space >= screenWidth) {
            return true;
        }
        float nowWidth = now.getContent().length() * now.getFontSize();
        float nowSpeed = (screenWidth + nowWidth) / showTime;
        if (nowSpeed > beforeSpeed) {
            //两者距离
            int distance = screenWidth - beforeLastPoint - space;
            //追尾时间
            float rearEndTime = distance / (nowSpeed - beforeSpeed);
            //在剩余时间内可能追及
            return rearEndTime < showTime-intervalTime;
        }
        return false;
    }

    /**
     * 内部缓存类
     */
    static class AssDanMuData {
        private long startTime;
        private String content;
        private float fontSize;

        public AssDanMuData(long startTime, String content, float fontSize) {
            this.startTime = startTime;
            this.content = content;
            this.fontSize = fontSize;
        }

        public AssDanMuData() {

        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public float getFontSize() {
            return fontSize;
        }

        public void setFontSize(float fontSize) {
            this.fontSize = fontSize;
        }
    }

    /**
     * 创建文件并写入
     * @param saveFile 将保存的文件对象
     * @return 创建完成的文件
     * @throws IOException 创建或写入时出现IO错误
     */
    private File createAssFileAndWriteHead(File saveFile) throws IOException {
        danMuFormatIndexMap.clear();
        for (int i = 0; i < sqliteFileSessionFactoryList.size(); i++) {
            SqlSessionFactory sf = sqliteFileSessionFactoryList.get(i);
            SqlSession sqlSession = sf.openSession();
            DanMuFormatModelMapper mapper = sqlSession.getMapper(DanMuFormatModelMapper.class);
            List<DanMuFormatModel> danMuFormatModelList = mapper.getAll();
            for (DanMuFormatModel dfm :
                    danMuFormatModelList) {
                danMuFormatIndexMap.put(i + "-" + dfm.getId(), dfm);
            }
            sqlSession.close();
        }
        //写入头部
        Files.writeString(saveFile.toPath(),
                createAssInfo() + createStylesString(danMuFormatIndexMap) + createEventHead(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return saveFile;
    }

    private String conventDanMuData(long videoStartTimeStamp,Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData[] beforeTempArray,Map<String, DanMuFormatModel> danMuFormatIndexMap,int sqlDataIndex, List<DanMuDataModel> danMuDataModelList,float trackWidth,int trackNum) {
        StringBuilder sb = new StringBuilder();
        if (danMuDataModelList != null && !danMuDataModelList.isEmpty()) {
            for (DanMuDataModel d : danMuDataModelList
            ) {
                long assDanMuStartTime = d.getCreateTime() - videoStartTimeStamp;
                DanMuFormat danMuFormat = danMuFormatIndexMap.get(sqlDataIndex + "-" + d.getFormat());
                Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData danMuData = new Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData(assDanMuStartTime, d.getData(), danMuFormat == null ? NORMAL_FONT_SIZE : danMuFormat.getFontSize());
                //TODO 增加其他样式弹出的弹幕样式支持
                //TODO 增加显示用户名功能
                //循环查找并分配轨道，轨道满载时当前弹幕抛弃
                for (int k = 0; k < trackNum; k++) {
                    Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData before = beforeTempArray[k];
                    //当前轨道无前置弹幕或判断在此轨道不会重叠弹幕时
                    if (before == null || !checkDanMuOverlap(videoWidth,before,danMuData,showTime)) {
                        //更新占位缓存
                        beforeTempArray[k] = danMuData;
                        sb.append("Dialogue:0,")
                                //时间
                                .append(FormatUtil.millTime2String(assDanMuStartTime)).append(",")
                                .append(FormatUtil.millTime2String(assDanMuStartTime + showTime)).append(",")
                                //样式
                                .append(danMuFormat == null ? NORMAL_STYLE_NAME : sqlDataIndex + "-" + d.getFormat()).append(",")
                                .append(",0000,0000,0000,,")
                                /*
                                 * \move(<x1>, <y1>, <x2>, <y2>[, <t1>, <t2>])
                                 * ①x1,y1为移动开始的位置，x2,y2为移动结束的位置；参照点(移动点)由对齐方式决定
                                 * ②t1,t2是移动开始和结束时间，单位 [ms]，缺省则在此字幕持续时间内进行移动
                                 * ③所有变量均可用小数
                                 * */
                                .append("{\\move(")
                                .append(videoWidth).append(",").append(k * trackWidth * 1.2).append(",")
                                .append(-danMuData.getContent().length() * danMuData.getFontSize()).append(",").append(k * trackWidth * 1.2)
                                .append(")}")
                                .append(danMuData.getContent())
                                .append("\r\n")
                        ;

                        break;
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * 读取服务类记录的数据库文件，读取并添加弹幕信息到ASS文件中
     *
     * @param videoStartTimeStamp 视频开始的时间戳，用于弹幕时间轴匹配
     * @param saveFile               ASS保存的文件对象
     * @param danMuDataModelSelector 弹幕数据的选择类，用于数据库筛选
     * @return 组装好的导出数据信息
     * @throws IOException 当写入ASS文件时出现问题
     */
    private DanMuExportDataInfo<File> appendDanMuData2AssFile(long videoStartTimeStamp, File saveFile, DanMuDataModelSelector danMuDataModelSelector) throws IOException {
        DanMuExportDataInfo<File> danMuExportDataInfo = new DanMuExportDataInfo<>();
        danMuExportDataInfo.setData(saveFile);
        //写入具体弹幕
        //允许的轨道数量
        float trackWidth = maxFontSize * 1.2f;
        int trackNum = (int) ((videoHeight * screenProp) / trackWidth);
        Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData[] beforeTempArray = new Sqlite2AssFileDanMuFormatExportServiceImpl.AssDanMuData[trackNum];
        //分页取出数量
        int pageSize = 1000;
        StringBuilder tempStringBuilder = new StringBuilder();
        //数据统计信息
        long totalDanMuNum = 0;
        long usageDanMuNum = 0;
        //对于多个数据源
        for (int i = 0; i < sqliteFileSessionFactoryList.size(); i++) {
            SqlSessionFactory sf = sqliteFileSessionFactoryList.get(i);
            SqlSession sqlSession = sf.openSession();
            DanMuDataModelMapper mapper = sqlSession.getMapper(DanMuDataModelMapper.class);
            //分页获取
            DataPage<DanMuDataModel> page = mapper.listPage(danMuDataModelSelector, 0, pageSize);
            totalDanMuNum += page.getTotal();
            //后续增加关键词过滤时，具体使用的数量可能需要调整（数据库过滤或程序过滤）
            usageDanMuNum += page.getTotal();
            int maxPage = page.getMaxPageNum();
            for (int j = 0; j <= maxPage; j++) {
                tempStringBuilder.append(conventDanMuData(videoStartTimeStamp, beforeTempArray, danMuFormatIndexMap, i, page.getData(), trackWidth, trackNum));
                //写入并清空缓存
                Files.writeString(saveFile.toPath(),
                        tempStringBuilder.toString(),
                        StandardOpenOption.APPEND);
                tempStringBuilder.delete(0, tempStringBuilder.length());
                //从数据库中读取更新数据
                page = mapper.listPage(new DanMuDataModelSelector(), j++, pageSize);
            }
            sqlSession.close();
        }
        danMuExportDataInfo.setTotalNum(totalDanMuNum);
        danMuExportDataInfo.setUsageNum(usageDanMuNum);
        return danMuExportDataInfo;
    }


    /**
     * 导出指定筛选的弹幕结果
     *
     * @param startTime 开始时间
     * @param endTime   结束时间,为null时为不限
     */
    @Override
    public DanMuExportDataInfo<File> formatExportBySelector(@NotNull LocalDateTime startTime, LocalDateTime endTime) throws IOException {
        //生成模式名称和相关筛选
        String modelName;
        DanMuDataModelSelector danMuDataModelSelector = new DanMuDataModelSelector();
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        //TODO 调整为毫秒级，B站相关数据库须做额外转换适配
        danMuDataModelSelector.setStartCreateTime(startTime.toInstant(offsetDateTime.getOffset()).toEpochMilli());
        //以记录包含时间命名
        modelName = "("+startTime.format(DateTimeFormatter.ofPattern(fileNameTimeFormat))+ "~";
        if (endTime != null) {
            danMuDataModelSelector.setEndCreateTime(endTime.toInstant(offsetDateTime.getOffset()).toEpochMilli());
            modelName = modelName + endTime.format(DateTimeFormatter.ofPattern(fileNameTimeFormat));
        } else {
            modelName = modelName + "now";
        }
        modelName = modelName + ")";
        //文件名
        String fileName = saveAssFilePath.getAbsolutePath() + File.separator
                + liveName + "-R-" + modelName
                + ".ass";
        File saveAssFile = new File(fileName);
        //创建文件和头部信息
        createAssFileAndWriteHead(saveAssFile);
        //时间戳
        long videoStartTimeStamp = startTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
        //读取并添加弹幕信息
        DanMuExportDataInfo<File> danMuExportDataInfo = appendDanMuData2AssFile(videoStartTimeStamp,saveAssFile,danMuDataModelSelector);
        return danMuExportDataInfo;
    }



}
