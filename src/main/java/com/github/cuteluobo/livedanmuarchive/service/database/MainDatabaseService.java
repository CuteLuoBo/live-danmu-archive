package com.github.cuteluobo.livedanmuarchive.service.database;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuAccountTaskSelector;
import com.github.cuteluobo.livedanmuarchive.dto.DanMuSenderTaskSelector;
import com.github.cuteluobo.livedanmuarchive.mapper.main.DanmuAccountTaskMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.main.DanmuSenderTaskMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.main.MainTableMapper;
import com.github.cuteluobo.livedanmuarchive.model.DanmuAccountTaskModel;
import com.github.cuteluobo.livedanmuarchive.model.DanmuSenderTaskModel;
import com.github.cuteluobo.livedanmuarchive.utils.DatabaseUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * 主数据库操作类
 * @author CuteLuoBo
 * @date 2023/3/15 10:42
 */
public class MainDatabaseService {
    private Logger logger = LoggerFactory.getLogger(MainDatabaseService.class);
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
        } catch (Exception e) {
            logger.error("执行数据库SQL语句失败",e);
            throw e;
        }
    }

    private void checkTime(DanmuSenderTaskModel danmuSenderTaskModel) {
        long nowTime = System.currentTimeMillis();
        if (danmuSenderTaskModel.getCreateTime() == null) {
            danmuSenderTaskModel.setCreateTime(nowTime);
        }
        if (danmuSenderTaskModel.getUpdateTime() == null) {
            danmuSenderTaskModel.setUpdateTime(System.currentTimeMillis());
        }
    }

    /**
     * 根据筛选器返回查询结果
     * @param selector 筛选器
     * @return 查询结果
     */
    public List<DanmuSenderTaskModel> getSenderTaskListBySelector(DanMuSenderTaskSelector selector) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            DanmuSenderTaskMapper danmuSenderTaskMapper = sqlSession.getMapper(DanmuSenderTaskMapper.class);
            return danmuSenderTaskMapper.selectListBySelector(selector);
        } catch (Exception e) {
            logger.error("执行数据库SQL语句失败",e);
            throw e;
        }
    }

    /**
     * 根据筛选器返回查询结果
     * @param selector 筛选器
     * @return 查询结果
     */
    public List<DanmuAccountTaskModel> getAccountTaskListBySelector(DanMuAccountTaskSelector selector) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            DanmuAccountTaskMapper danmuAccountTaskMapper = sqlSession.getMapper(DanmuAccountTaskMapper.class);
            return danmuAccountTaskMapper.selectListBySelector(selector);
        } catch (Exception e) {
            logger.error("执行数据库SQL语句失败",e);
            throw e;
        }
    }

    /**
     * 查询未完成的单个结果
     * @param senderTaskId 主任务ID
     * @param senderUid    发送者UID
     * @param videoId      视频ID
     * @param stop         是否中止
     * @return 查询结果
     */
    public DanmuAccountTaskModel getAccountTaskByNoFinish(int senderTaskId, String senderUid, String videoId, Boolean stop) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            DanmuAccountTaskMapper danmuAccountTaskMapper = sqlSession.getMapper(DanmuAccountTaskMapper.class);
            return danmuAccountTaskMapper.selectOneByNoFinish(senderTaskId, senderUid, videoId, stop);
        } catch (Exception e) {
            logger.error("执行数据库SQL语句失败",e);
            throw e;
        }
    }

    /**
     * 更新单个结果
     * @param danmuAccountTaskModel 弹幕账户任务数据
     * @return 更新数量
     */
    public int updateAccountTask(DanmuAccountTaskModel danmuAccountTaskModel) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            DanmuAccountTaskMapper danmuAccountTaskMapper = sqlSession.getMapper(DanmuAccountTaskMapper.class);
            return danmuAccountTaskMapper.updateByPrimaryKeySelective(danmuAccountTaskModel);
        } catch (Exception e) {
            logger.error("执行数据库SQL语句失败",e);
            throw e;
        }
    }

    /**
     * 新建单个结果
     * @param danmuAccountTaskModel 弹幕账户任务数据
     * @return 更新数量
     */
    public int addAccountTask(DanmuAccountTaskModel danmuAccountTaskModel) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            DanmuAccountTaskMapper danmuAccountTaskMapper = sqlSession.getMapper(DanmuAccountTaskMapper.class);
            return danmuAccountTaskMapper.insert(danmuAccountTaskModel);
        } catch (Exception e) {
            logger.error("执行数据库SQL语句失败",e);
            throw e;
        }
    }

    /**
     * 更新弹幕任务数据
     * @param danmuSenderTaskModel 数据
     * @return 更新数量
     */
    public int updateDanMuSenderTask(DanmuSenderTaskModel danmuSenderTaskModel) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            DanmuSenderTaskMapper danmuSenderTaskMapper = sqlSession.getMapper(DanmuSenderTaskMapper.class);
            return danmuSenderTaskMapper.updateByPrimaryKeySelective(danmuSenderTaskModel);
        } catch (Exception e) {
            logger.error("执行数据库SQL语句失败",e);
            throw e;
        }
    }


    /**
     * 根据flag筛选列表
     * @param skip  是否跳过
     * @param fail  是否失败
     * @param finish 是否已经完成
     * @param limit 限制数量
     * @return 查询结果
     */
    public List<DanmuSenderTaskModel> getListByFlag(boolean skip,boolean fail,boolean finish,int limit) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            DanmuSenderTaskMapper danmuSenderTaskMapper = sqlSession.getMapper(DanmuSenderTaskMapper.class);
            return danmuSenderTaskMapper.selectListByFlag(skip, fail, finish, limit);
        } catch (Exception e) {
            logger.error("执行数据库SQL语句失败",e);
            throw e;
        }
    }

    /**
     * 获得全部结果
     * @return 查询结果
     */
    public List<DanmuSenderTaskModel> getAllList() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            DanmuSenderTaskMapper danmuSenderTaskMapper = sqlSession.getMapper(DanmuSenderTaskMapper.class);
            return danmuSenderTaskMapper.selectAll();
        } catch (Exception e) {
            logger.error("执行数据库SQL语句失败",e);
            throw e;
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
        } catch (Exception e) {
            logger.error("执行数据库SQL语句失败",e);
            throw e;
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
            return danmuSenderTaskMapper.selectLatestOneByCreatorUid(creatorUid);
        } catch (Exception e) {
            logger.error("执行数据库SQL语句失败",e);
            throw e;
        }
    }

    /**
     * 获得视频创建时间最新的一个结果
     *
     * @param platform   平台
     * @param skip       是否跳过
     * @param fail       是否错误
     * @param creatorUid 视频创建者UID
     * @param videoId    视频ID
     * @return 查询结果
     */
    public DanmuSenderTaskModel getOneLatest(String platform, boolean skip, boolean fail, String creatorUid, String videoId) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            DanmuSenderTaskMapper danmuSenderTaskMapper = sqlSession.getMapper(DanmuSenderTaskMapper.class);
            return danmuSenderTaskMapper.selectOneLatest(platform,skip,fail,creatorUid,videoId);
        } catch (Exception e) {
            logger.error("执行数据库SQL语句失败",e);
            throw e;
        }
    }

}
