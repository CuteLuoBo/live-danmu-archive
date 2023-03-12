package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuFormatModelMapper;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuFormatModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuUserInfoModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuExportDataInfo;
import com.github.cuteluobo.livedanmuarchive.pojo.FormatDanMuData;
import com.github.cuteluobo.livedanmuarchive.service.DanMuFormatExportService;
import com.github.cuteluobo.livedanmuarchive.utils.DatabaseConfigUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用sqlite储存的弹幕数据导出服务
 * @author CuteLuoBo
 * @date 2022/11/24 14:52
 */
public abstract class SqliteDanMuFormatExportService<T> implements DanMuFormatExportService<T> {
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
    public final String NORMAL_STYLE_FONT = "黑体";

    /**
     * 默认样式的名称
     */
    public final String NORMAL_STYLE_NAME = "normal";

    /**
     * 默认字体大小
     */
    public final float NORMAL_FONT_SIZE = 25.0f;

    /**
     * 文件时间命名格式
     */
    public final String fileNameTimeFormat = "yyyy-MM-dd HH-mm-ss";


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

    /**
     * 弹幕发送用户缓存map
     */
    private Map<String, DanMuUserInfoModel> danMuUserInfoModelHashMap = new HashMap<>(32);



    public SqliteDanMuFormatExportService(@NotNull List<File> sqliteFileList) throws ServiceException {
        this.sqliteFileList = sqliteFileList;

        //生成对应访问工厂
        sqliteFileSessionFactoryList = new ArrayList<>(sqliteFileList.size());
        if (sqliteFileList.isEmpty()) {
            throw new ServiceException("传入的Sqlite列表不能为空");
        }
        //遍历创建对话工厂
        for (File f :
                sqliteFileList) {
            if (!f.exists()) {
                throw new ServiceException("传入的Sqlite文件不存在");
            }
            sqliteFileSessionFactoryList.add(DatabaseConfigUtil.initFileDatabaseConnectFactory(f));
        }
        reloadDanMuFormatTempMap();
    }

    /**
     * 重载数据弹幕缓存map
     */
    protected void reloadDanMuFormatTempMap() {
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
    }

    /**
     * 填入缓存用户数据
     * @param sqliteIndex            sqlite索引
     * @param danMuUserInfoModelList 关联的弹幕数据
     */
    protected void appendDanMuUserInfoTemp(int sqliteIndex, List<DanMuUserInfoModel> danMuUserInfoModelList) {
        if (danMuUserInfoModelHashMap == null) {
            danMuUserInfoModelHashMap = new HashMap<>(danMuUserInfoModelList.size());
        }
        for (DanMuUserInfoModel d :
                danMuUserInfoModelList) {
            danMuUserInfoModelHashMap.put("i" + "-" + d.getId(), d);
        }
    }

    /**
     * 获取缓存的用户数据
     * @param sqliteIndex            sqlite索引
     * @param userId                 用户ID
     * @return 用户数据
     */
    protected DanMuUserInfoModel getDanMuUserInfoTempByIndexAndId(int sqliteIndex, int userId) {
        return danMuUserInfoModelHashMap.get(sqliteIndex + "-" + userId);
    }

    /**
     * 判断弹幕是否会重叠
     * @param screenWidth  屏幕宽度
     * @param before       之前的弹幕
     * @param now          当前计算弹幕
     * @param allowSpace   弹幕间隔
     * @param showTime     弹幕展现时间(ms)
     * @return 判断结果
     */
    public static boolean checkDanMuOverlap(int screenWidth, FormatDanMuData before, FormatDanMuData now,int allowSpace, int showTime) {
        //检查间隔时间
        long intervalTime = now.getStartTime() - before.getStartTime();
        if (intervalTime > showTime) {
            return false;
        }
        float beforeWidth = before.getContent().length() * before.getFontSize();
        float beforeSpeed = (screenWidth + beforeWidth) / showTime;
        //之前弹幕的尾部坐标
        int beforeLastPoint = (int) (screenWidth + beforeWidth - (beforeSpeed * intervalTime) );
        //之前弹幕仍未完全显示完成
        if (beforeLastPoint + allowSpace >= screenWidth) {
            return true;
        }
        float nowWidth = now.getContent().length() * now.getFontSize();
        float nowSpeed = (screenWidth + nowWidth) / showTime;
        if (nowSpeed > beforeSpeed) {
            //两者距离
            int distance = screenWidth - beforeLastPoint - allowSpace;
            //追尾时间
            float rearEndTime = distance / (nowSpeed - beforeSpeed);
            //在剩余时间内可能追及
            return rearEndTime < showTime-intervalTime;
        }
        return false;
    }

    public List<File> getSqliteFileList() {
        return sqliteFileList;
    }

    public void setSqliteFileList(List<File> sqliteFileList) {
        this.sqliteFileList = sqliteFileList;
    }

    public List<SqlSessionFactory> getSqliteFileSessionFactoryList() {
        return sqliteFileSessionFactoryList;
    }

    public void setSqliteFileSessionFactoryList(List<SqlSessionFactory> sqliteFileSessionFactoryList) {
        this.sqliteFileSessionFactoryList = sqliteFileSessionFactoryList;
    }

    public int getMaxFontSize() {
        return maxFontSize;
    }

    public void setMaxFontSize(int maxFontSize) {
        this.maxFontSize = maxFontSize;
    }

    public int getMixFontSize() {
        return mixFontSize;
    }

    public void setMixFontSize(int mixFontSize) {
        this.mixFontSize = mixFontSize;
    }

    public boolean isShowSenderName() {
        return showSenderName;
    }

    public void setShowSenderName(boolean showSenderName) {
        this.showSenderName = showSenderName;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public String getFileNameTimeFormat() {
        return fileNameTimeFormat;
    }


    public float getScreenProp() {
        return screenProp;
    }

    public void setScreenProp(float screenProp) {
        this.screenProp = screenProp;
    }

    public int getShowTime() {
        return showTime;
    }

    public void setShowTime(int showTime) {
        this.showTime = showTime;
    }

    public Map<String, DanMuFormatModel> getDanMuFormatIndexMap() {
        return danMuFormatIndexMap;
    }

    public void setDanMuFormatIndexMap(Map<String, DanMuFormatModel> danMuFormatIndexMap) {
        this.danMuFormatIndexMap = danMuFormatIndexMap;
    }

    public Map<String, DanMuUserInfoModel> getDanMuUserInfoModelHashMap() {
        return danMuUserInfoModelHashMap;
    }

    public void setDanMuUserInfoModelHashMap(Map<String, DanMuUserInfoModel> danMuUserInfoModelHashMap) {
        this.danMuUserInfoModelHashMap = danMuUserInfoModelHashMap;
    }


}
