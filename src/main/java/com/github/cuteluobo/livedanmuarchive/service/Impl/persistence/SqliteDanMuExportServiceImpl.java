package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import cn.hutool.core.bean.BeanUtil;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuDatabaseConstant;
import com.github.cuteluobo.livedanmuarchive.enums.ExportPattern;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuMessageType;
import com.github.cuteluobo.livedanmuarchive.manager.FileExportManager;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuDataModelMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuDatabaseTableMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuFormatModelMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuUserInfoModelMapper;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuFormatModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuUserInfoModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuFormat;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuUserInfo;
import com.github.cuteluobo.livedanmuarchive.service.AbstractFilesDanMuExportService;
import com.github.cuteluobo.livedanmuarchive.service.ExDanMuExportService;
import com.github.cuteluobo.livedanmuarchive.utils.DatabaseConfigUtil;
import com.github.cuteluobo.livedanmuarchive.utils.MybatisUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.mapping.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.ServerException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SQLite格式弹幕数据导出
 * @author CuteLuoBo
 * @date 2022/4/3 18:01
 */
public class SqliteDanMuExportServiceImpl extends AbstractFilesDanMuExportService implements ExDanMuExportService {
    Logger logger = LoggerFactory.getLogger(SqliteDanMuExportServiceImpl.class);

    /**
     * 缓存当前使用的数据库文件
     */
    private File nowUsageDatabaseFile;
    /**
     * 缓存当前可用的sql对话工厂
     */
    private SqlSessionFactory nowUsageSqlSessionFactory;

    /**
     * 是否有检查表存在（初始化）
     */
    private Boolean checkTableExist = false;

    private Boolean isNewDatabaseFile = true;

    public SqliteDanMuExportServiceImpl(String liveName, ExportPattern danMuExportPattern) throws IOException {
        super(liveName, danMuExportPattern);
        //最大尝试初始化次数
        int initMaxNum = 3;
        int nowNum = 0;
        //循环尝试初始化
        while (true) {
            initDatasourceConfig();
            nowUsageSqlSessionFactory = MybatisUtil.initFileDatabaseConnectFactory(nowUsageDatabaseFile);
            checkAndCreateTable();
            //TODO 后续考虑根据各步骤错误,进行单独针对的重试
            if (checkTableExist) {
                break;
            } else if (nowNum >= initMaxNum) {
                logger.error("数据库初始化超过最大重试次数:{},服务初始化中止",initMaxNum);
                throw new ServerException("数据库初始化超过最大重试次数");
            }else {
                nowNum++;
                logger.warn("数据库初始化失败,正在重试,当前尝试次数:{}",nowNum);
            }
        }
    }

    /**
     * 单弹幕导出操作
     *
     * @param danMuData 弹幕信息
     * @return 是否导出成功
     * @throws IOException 导出时发生的IO错误
     */
    @Override
    public Boolean export(DanMuData danMuData) throws IOException {
        if (danMuData == null) {
            logger.warn("接收到空白消息，跳过本次导出");
            return false;
        }
        try(SqlSession sqlSession = nowUsageSqlSessionFactory.openSession()){
            DanMuDataModelMapper danMuDataModelMapper = sqlSession.getMapper(DanMuDataModelMapper.class);
            DanMuUserInfoModelMapper danMuUserInfoModelMapper = sqlSession.getMapper(DanMuUserInfoModelMapper.class);
            DanMuFormatModelMapper danMuFormatModelMapper = sqlSession.getMapper(DanMuFormatModelMapper.class);
            //用户数据
            DanMuUserInfo danMuUserInfo = danMuData.getUserIfo();
            DanMuUserInfoModel danMuUserInfoModel = new DanMuUserInfoModel();
            if (danMuUserInfo != null) {
                DanMuUserInfoModel dataBaseModel = danMuUserInfoModelMapper.getOneByNickName(danMuUserInfo.getNickName());
                if (dataBaseModel == null) {
                    //拷贝数据
                    BeanUtil.copyProperties(danMuUserInfo,danMuUserInfoModel);
                    danMuUserInfoModel.setAddTime(System.currentTimeMillis());
                    danMuUserInfoModelMapper.addOne(danMuUserInfoModel);
                }else {
                    danMuUserInfoModel = dataBaseModel;
                }
            }else {
                //缺失时，填入默认数据
            }

            //弹幕样式
            DanMuFormat danMuFormat = danMuData.getDanMuFormatData();
            DanMuFormatModel danMuFormatModel = new DanMuFormatModel();
            if (danMuFormat != null) {
                //拷贝数据
                BeanUtil.copyProperties(danMuFormat,danMuFormatModel);
                List<DanMuFormatModel> danMuFormatModelList = danMuFormatModelMapper.getListByModel(danMuFormatModel);
                if (danMuFormatModelList == null || danMuFormatModelList.size() == 0) {
                    danMuFormatModelMapper.addOne(danMuFormatModel);
                }else{
                    danMuFormatModel = danMuFormatModelList.get(0);
                }
            }else{
                //缺失时，填入默认数据
            }

            if (danMuData.getContent() != null) {
                //弹幕主体数据组装并写入数据
                DanMuDataModel danMuDataModel = new DanMuDataModel();
                danMuDataModel.setData(danMuData.getContent());
                danMuDataModel.setCreateTime(danMuData.getTimestamp());
                danMuDataModel.setFormat(danMuFormatModel.getId());
                danMuDataModel.setUserId(danMuUserInfoModel.getId());
                danMuDataModel.setType(DanMuMessageType.getEnumByValue(danMuData.getMsgType()).getTypeValue());
                danMuDataModelMapper.addOne(danMuDataModel);
            }
            sqlSession.commit();
        }
        return true;
    }

    /**
     * 初始化数据源配置
     */
    private void initDatasourceConfig() {
        logger.info("任务：{},正在尝试初始化配置SQLite数据源...",getLiveName());
        FileExportManager fileExportManager = FileExportManager.getInstance();
        //拼接文件导出文件夹:{定义的总输出路径}/{主播名称}/danmu
        File exportDirPath = new File(fileExportManager.getExportDir().getAbsolutePath()+File.separator+getLiveName()+File.separator+"danmu");
        //是否需要检查旧文件
        boolean checkOldFile = true;
        //目录不存在时进行创建
        if (!exportDirPath.exists()) {
            exportDirPath.mkdirs();
            checkOldFile = false;
        }
        // 集中模式(当储存总文件存在时，)
        if (ExportPattern.ALL_COLLECT == getDanMuExportPattern()) {
            logger.info("任务：{},数据储存模式:集中模式",getLiveName());
            //可能有旧文件模式
            if (checkOldFile) {
                logger.info("尝试获取旧的数据库文件,过滤模式：{}", "*" + getLiveName() + "*.db");
                //文件名过滤器
                FilenameFilter filenameFilter = (file, name) -> name.endsWith(".db") && name.replace(".db", "").contains(getLiveName());
                File[] files = exportDirPath.listFiles(filenameFilter);
                if (files != null && files.length > 0) {
                    logger.debug("过滤获得的旧数据库文件:{}", Arrays.stream(files).map(File::getName).collect(Collectors.joining("\\r\\n")));
                    //使用过滤的第一个文件
                    logger.info("使用此文件进行储存:{}",files[0].getName());
                    nowUsageDatabaseFile = files[0];
                    if (nowUsageDatabaseFile.exists()) {
                        isNewDatabaseFile = false;
                    }
                    return;
                }
            }
            //无旧文件模式,需要创建
            LocalDateTime localDateTime = LocalDateTime.now();
            String fileName = getLiveName() + localDateTime.format(getNormalDateTimeFormatter());
            setSaveFileName(fileName);
            //当前使用文件名
            nowUsageDatabaseFile = new File(exportDirPath.getAbsolutePath() + File.separator + fileName + ".db");
            logger.info("没有找的旧数据库文件，将进行新建，路径：{}",nowUsageDatabaseFile.getAbsolutePath());
            if (nowUsageDatabaseFile.exists()) {
                isNewDatabaseFile = false;
            }
        }
        //文件按日期文件夹放置模式
        if (ExportPattern.DAY_FOLDER == getDanMuExportPattern()) {
            logger.info("任务：{},数据储存模式:日期分割模式",getLiveName());
            LocalDateTime localDateTime = LocalDateTime.now();
            //新的导出路径 = {导出路径}/{日期}
            exportDirPath = new File(exportDirPath.getAbsolutePath() + File.separator
                    + localDateTime.format(getNormalDateFormatter()));
            //无文件夹时进行创建
            if (!exportDirPath.exists()) {
                exportDirPath.mkdirs();
            }
            //文件名 = {主播名称}--{格式化时间}
            String fileName = getLiveName() +"--"+ localDateTime.format(getNormalDateTimeFormatter());
            setSaveFileName(fileName);
            //当前使用文件
            nowUsageDatabaseFile = new File(exportDirPath.getAbsolutePath() + File.separator + fileName + ".db");
            logger.info("没有找的旧数据库文件，将进行新建，路径：{}",nowUsageDatabaseFile.getAbsolutePath());
            if (nowUsageDatabaseFile.exists()) {
                isNewDatabaseFile = false;
            }
        }
    }

    /**
     * 检查和创建表
     *
     */
    private void checkAndCreateTable() {
        //创建sql对话
        try (SqlSession sqlSession = nowUsageSqlSessionFactory.openSession()) {
            DanMuDatabaseTableMapper danMuDatabaseTableMapper = sqlSession.getMapper(DanMuDatabaseTableMapper.class);
            //新数据库时直接创建，否则验证
            if (isNewDatabaseFile) {
                logger.debug("判断为新创建数据库，略过检验直接建表");
                danMuDatabaseTableMapper.createUserInfoTable();
                danMuDatabaseTableMapper.createDanmuDataTable();
                danMuDatabaseTableMapper.createDanmuFormatTable();
            }else{
                logger.debug("判断为旧数据库，检查表是否存在");
                //校验表
                try {
                    if (danMuDatabaseTableMapper.checkTableExistBySqlite(DanMuDatabaseConstant.TABLE_USER_INFO.getValue()) == 0) {
                        danMuDatabaseTableMapper.createUserInfoTable();
                    }
                    if (danMuDatabaseTableMapper.checkTableExistBySqlite(DanMuDatabaseConstant.TABLE_DANMU_DATA.getValue()) == 0) {
                        danMuDatabaseTableMapper.createDanmuDataTable();
                    }
                    if (danMuDatabaseTableMapper.checkTableExistBySqlite(DanMuDatabaseConstant.TABLE_DANMU_FORMAT.getValue()) == 0) {
                        danMuDatabaseTableMapper.createDanmuFormatTable();
                    }
                } catch (Exception exception) {
                    logger.error("旧数据库表检验与新建失败");
                    throw exception;
                }
                logger.debug("表检验成功");
            }
            sqlSession.commit();
            checkTableExist = true;
        }

    }

    /**
     * 批量导出
     *
     * @param danMuDataList 弹幕信息列表
     * @return 是否导出成功
     */
    @Override
    public Boolean batchExport(List<DanMuData> danMuDataList) {
        if (danMuDataList == null || danMuDataList.isEmpty()) {
            logger.warn("接收到空白消息，跳过本次导出");
            return false;
        }
        try(SqlSession sqlSession = nowUsageSqlSessionFactory.openSession()){
            DanMuDataModelMapper danMuDataModelMapper = sqlSession.getMapper(DanMuDataModelMapper.class);
            DanMuUserInfoModelMapper danMuUserInfoModelMapper = sqlSession.getMapper(DanMuUserInfoModelMapper.class);
            DanMuFormatModelMapper danMuFormatModelMapper = sqlSession.getMapper(DanMuFormatModelMapper.class);
            //TODO 目前看单个串行处理可避免出错，后续考虑联表批量插入
            for (DanMuData danMuData :
                    danMuDataList) {
                //用户数据
                DanMuUserInfo danMuUserInfo = danMuData.getUserIfo();
                DanMuUserInfoModel danMuUserInfoModel = new DanMuUserInfoModel();
                if (danMuUserInfo != null) {
                    DanMuUserInfoModel dataBaseModel = danMuUserInfoModelMapper.getOneByNickName(danMuUserInfo.getNickName());
                    if (dataBaseModel == null) {
                        //拷贝数据
                        BeanUtil.copyProperties(danMuUserInfo,danMuUserInfoModel);
                        danMuUserInfoModel.setAddTime(System.currentTimeMillis());
                        danMuUserInfoModelMapper.addOne(danMuUserInfoModel);
                    }else {
                        danMuUserInfoModel = dataBaseModel;
                    }
                }else {
                    //缺失时，填入默认数据
                }

                //弹幕样式
                DanMuFormat danMuFormat = danMuData.getDanMuFormatData();
                DanMuFormatModel danMuFormatModel = new DanMuFormatModel();
                if (danMuFormat != null) {
                    //拷贝数据
                    BeanUtil.copyProperties(danMuFormat,danMuFormatModel);
                    List<DanMuFormatModel> danMuFormatModelList = danMuFormatModelMapper.getListByModel(danMuFormatModel);
                    if (danMuFormatModelList == null || danMuFormatModelList.size() == 0) {
                        danMuFormatModelMapper.addOne(danMuFormatModel);
                    }else{
                        danMuFormatModel = danMuFormatModelList.get(0);
                    }
                }else{
                    //缺失时，填入默认数据
                }

                if (danMuData.getContent() != null) {
                    //弹幕主体数据组装并写入数据
                    DanMuDataModel danMuDataModel = new DanMuDataModel();
                    danMuDataModel.setData(danMuData.getContent());
                    danMuDataModel.setCreateTime(danMuData.getTimestamp());
                    danMuDataModel.setFormat(danMuFormatModel.getId());
                    danMuDataModel.setUserId(danMuUserInfoModel.getId());
                    danMuDataModel.setType(DanMuMessageType.getEnumByValue(danMuData.getMsgType()).getTypeValue());
                    danMuDataModelMapper.addOne(danMuDataModel);
                }
            }
            sqlSession.commit();
        }
        return true;
    }
}
