package com.github.cuteluobo.livedanmuarchive.service.database;

import com.github.cuteluobo.livedanmuarchive.mapper.main.DanmuSenderTaskMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.main.MainTableMapper;
import com.github.cuteluobo.livedanmuarchive.model.DanmuSenderTaskModel;
import com.github.cuteluobo.livedanmuarchive.utils.DatabaseUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * 主数据库操作类
 * @author CuteLuoBo
 * @date 2023/3/15 10:42
 */
public class MainDatabaseService {
    private String dbFileName = "mainDatabase.db";
    private File dbFile = new File(dbFileName);
    private SqlSessionFactory sqlSessionFactory;

    private MainDatabaseService() {
        //初始化数据库
        boolean isNewFile = !dbFile.exists();
        sqlSessionFactory = DatabaseUtil.initFileDatabaseConnectFactory(dbFile, DatabaseUtil.MAIN_DATABASE_MODEL_LIST, DatabaseUtil.MAIN_DATABASE_MAPPER_LIST);
        //检查并创建表
        DatabaseUtil.checkAndCreateTable(sqlSessionFactory, isNewFile, MainTableMapper.class);
    }

    public static MainDatabaseService getInstance() {
        return InstanceClass.INSTANCE;
    }

    private static class InstanceClass{
        private final static MainDatabaseService INSTANCE = new MainDatabaseService();
    }

    public int addSenderTask(@NotNull DanmuSenderTaskModel danmuSenderTaskModel) {
        //检查时间
        checkTime(danmuSenderTaskModel);
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            DanmuSenderTaskMapper danmuSenderTaskMapper = sqlSession.getMapper(DanmuSenderTaskMapper.class);
            return danmuSenderTaskMapper.insert(danmuSenderTaskModel);
        }
    }

    private void checkTime(DanmuSenderTaskModel danmuSenderTaskModel) {
        long nowTime = System.currentTimeMillis();
        if (danmuSenderTaskModel.getCreatedTime() == null) {
            danmuSenderTaskModel.setCreatedTime(nowTime);
        }
        if (danmuSenderTaskModel.getUpdateTime() == null) {
            danmuSenderTaskModel.setUpdateTime(System.currentTimeMillis());
        }
    }

    /**
     * 获得创建者UID相关的列表
     * @param creatorUid 视频创建者UID
     * @return 查询结果
     */
    public List<DanmuSenderTaskModel> getSenderTaskListByCreatorUid(String creatorUid) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            DanmuSenderTaskMapper danmuSenderTaskMapper = sqlSession.getMapper(DanmuSenderTaskMapper.class);
            return danmuSenderTaskMapper.selectListByCreatorUid(creatorUid);
        }
    }

    /**
     * 获得最新的一个结果
     * @param creatorUid 视频创建者UID
     * @return 查询结果
     */
    public DanmuSenderTaskModel getLatestOneByCreatorUid(String creatorUid) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            DanmuSenderTaskMapper danmuSenderTaskMapper = sqlSession.getMapper(DanmuSenderTaskMapper.class);
            danmuSenderTaskMapper.selectLatestOneByCreatorUid(creatorUid);
            return danmuSenderTaskMapper.selectLatestOneByCreatorUid(creatorUid);
        }
    }

    //TODO 完成定时监测任务服务，然后根据任务调用，创建此服务类方法
}
