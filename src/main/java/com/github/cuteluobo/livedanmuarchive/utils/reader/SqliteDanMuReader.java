package com.github.cuteluobo.livedanmuarchive.utils.reader;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuDataModelMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuFormatModelMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuUserInfoModelMapper;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuFormatModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuUserInfoModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DataPage;
import com.github.cuteluobo.livedanmuarchive.utils.DatabaseUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * sqlite查询的弹幕读取类
 *
 * @author CuteLuoBo
 * @date 2023/3/9 12:29
 */
public class SqliteDanMuReader {
    private File saveFile;
    private SqlSessionFactory sqlSessionFactory;

    private Map<Integer, DanMuFormatModel> formatModelMap = new HashMap<>();

//    private Map<Integer, DanMuUserInfoModel> userInfoModelMap = new HashMap<>();

    public SqliteDanMuReader(File saveFile) {
        this.saveFile = saveFile;
        this.sqlSessionFactory = DatabaseUtil.initFileDatabaseConnectFactory(
                saveFile,
                DatabaseUtil.DANMU_DATABASE_MODEL_LIST,
                DatabaseUtil.DANMU_DATABASE_MAPPER_LIST
        );
        reloadTempMap();
    }

    public SqliteDanMuReader(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
        reloadTempMap();
    }

    /**
     * 刷新缓存map
     */
    private void reloadTempMap() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DanMuFormatModelMapper danMuFormatModelMapper = session.getMapper(DanMuFormatModelMapper.class);
            List<DanMuFormatModel> danMuFormatModelList = danMuFormatModelMapper.getAll();
            formatModelMap = danMuFormatModelList.stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(entry.getId(), entry))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            //用户信息过多，直接从数据库获取而不缓存
//        List<DanMuUserInfoModel> danMuUserInfoModelList = danMuUserInfoModelMapper.getAll();
//        userInfoModelMap = danMuUserInfoModelList.stream()
//                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getId(), entry))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    /**
     * 统计结果数量
     *
     * @param danMuDataModelSelector  筛选条件
     * @return 查询结果
     */
    public int countNum(DanMuDataModelSelector danMuDataModelSelector) {
        //初始化session与mapper
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DanMuDataModelMapper danMuDataModelMapper = session.getMapper(DanMuDataModelMapper.class);
            int total = danMuDataModelMapper.countNum(danMuDataModelSelector);
            session.close();
            return total;
        }
    }

    /**
     * 读取指定筛选条件的所有数据列表
     *
     * @param danMuDataModelSelector  筛选条件
     * @return 查询结果
     */
    public List<DanMuData> readAll(DanMuDataModelSelector danMuDataModelSelector) {
        //初始化session与mapper
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DanMuDataModelMapper danMuDataModelMapper = session.getMapper(DanMuDataModelMapper.class);
            List<DanMuDataModel> danMuDataModelList = danMuDataModelMapper.listModelBySelector(danMuDataModelSelector);
            return conventList(danMuDataModelList);
        }
    }

    /**
     * 读取指定筛选条件的所有数据列表
     *
     * @param danMuDataModelSelector  筛选条件
     * @param current                 当前访问页数
     * @param pageSize                每页的数据量
     * @return 查询结果
     */
    public List<DanMuData> readListByPage(DanMuDataModelSelector danMuDataModelSelector,int current,int pageSize) {
        //初始化session与mapper
        List<DanMuDataModel> danMuDataModelList;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DanMuDataModelMapper danMuDataModelMapper = session.getMapper(DanMuDataModelMapper.class);
            danMuDataModelList = danMuDataModelMapper.listPageInternal(danMuDataModelSelector, current, pageSize);
        }
        return conventList(danMuDataModelList);
    }

    /**
     * 读取指定筛选条件的所有数据列表
     * @param danMuDataModelSelector 筛选条件
     * @param current 当前访问页数
     * @param interval 间隔时间，单位毫秒(建议20000)
     * @return 查询结果
     */
    public List<DanMuData> readListByTime(DanMuDataModelSelector danMuDataModelSelector,int current,long interval) {
        //初始化session与mapper
        List<DanMuDataModel> danMuDataModelList;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DanMuDataModelMapper danMuDataModelMapper = session.getMapper(DanMuDataModelMapper.class);
            danMuDataModelList = danMuDataModelMapper.listPageByTime(danMuDataModelSelector, current, interval);
        }
        return conventList(danMuDataModelList);
    }

    /**
     * 通过时间范围查询弹幕数据（仅包含ID及时间）
     * @param danMuDataModelSelector    时间筛选条件
     * @return 查询结果（仅包含ID及时间）
     */
    public List<DanMuDataModel> listTimeDataByTime(DanMuDataModelSelector danMuDataModelSelector) {
        //初始化session与mapper
        List<DanMuDataModel> danMuDataModelList;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DanMuDataModelMapper danMuDataModelMapper = session.getMapper(DanMuDataModelMapper.class);
            danMuDataModelList = danMuDataModelMapper.listTimeDataByTime(danMuDataModelSelector);
        }
        return danMuDataModelList;
    }
    /**
     * 读取指定筛选条件的所有数据列表
     *
     * @param danMuDataModelSelector  筛选条件
     * @param current                 当前访问页数
     * @param pageSize                每页的数据量
     * @return 查询结果
     */
    public DataPage<DanMuData> readPage(DanMuDataModelSelector danMuDataModelSelector,int current,int pageSize) {
        //初始化session与mapper
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DanMuDataModelMapper danMuDataModelMapper = session.getMapper(DanMuDataModelMapper.class);
            DataPage<DanMuDataModel> danMuDataModelPage = danMuDataModelMapper.listPage(danMuDataModelSelector, current, pageSize);
            return DataPage.convent(danMuDataModelPage, conventList(danMuDataModelPage.getData()));
        }
    }

    /**
     * 查询符合id-创建时间的弹幕数据
     * @param idMap     id-创建时间映射表
     * @return 查询结果
     */
    public List<DanMuData> listModelByIdAndCreateTimeMap(Map<Long, Long> idMap){
        if (idMap == null || idMap.isEmpty()) {
            return new ArrayList<>(0);
        }
        //初始化session与mapper
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DanMuDataModelMapper danMuDataModelMapper = session.getMapper(DanMuDataModelMapper.class);
            List<DanMuDataModel> danMuDataModelList = danMuDataModelMapper.listModelByIdAndCreateTimeMap(idMap);
            return conventList(danMuDataModelList);
        }
    }

    /**
     * 使用本地缓存转换为封装好的数据对象
     * @param danMuDataModelList 数据库原始数据
     * @return 转换结果
     */
    public List<DanMuData> conventList(List<DanMuDataModel> danMuDataModelList) {

        List<DanMuData> danMuDataList;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DanMuUserInfoModelMapper danMuUserInfoModelMapper = session.getMapper(DanMuUserInfoModelMapper.class);
            //TODO 待优化：可以一次性获取对应用户数据，并填入
//            Set<Integer> userIdList = danMuDataModelList.stream().map(DanMuDataModel::getUserId).collect(Collectors.toSet());

            if (danMuDataModelList != null) {
                danMuDataList = new ArrayList<>(danMuDataModelList.size());
                danMuDataModelList.forEach(danMuDataModel ->
                        danMuDataList.add(
                                DanMuData.convent(
                                        danMuDataModel,
                                        danMuUserInfoModelMapper.getOneById(danMuDataModel.getUserId()),
                                        formatModelMap.get(danMuDataModel.getFormat())
                                )
                        )
                );
            } else {
                danMuDataList = new ArrayList<>(0);
            }
            return danMuDataList;
        }
    }

    /**
     * 获得最早创建的数据
     * @return 查询结果，可能为null
     */
    public DanMuDataModel getCreateTimeFirst() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DanMuDataModelMapper danMuDataModelMapper = session.getMapper(DanMuDataModelMapper.class);
            return danMuDataModelMapper.getOneByCreateTimeOrder(false);
        }
    }

    /**
     * 根据ID获取结果
     * @param id 记录ID
     * @return 查询结果，可能为null
     */
    public DanMuDataModel getOneById(long id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DanMuDataModelMapper danMuDataModelMapper = session.getMapper(DanMuDataModelMapper.class);
            return danMuDataModelMapper.getOneById(id);
        }
    }

    /**
     * 获得最后创建的数据结果
     * @return 查询结果，可能为null
     */
    public DanMuDataModel getCreateTimeEnd() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DanMuDataModelMapper danMuDataModelMapper = session.getMapper(DanMuDataModelMapper.class);
            return danMuDataModelMapper.getOneByCreateTimeOrder(true);
        }
    }
}
