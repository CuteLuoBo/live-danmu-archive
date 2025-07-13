package com.github.cuteluobo.livedanmuarchive.utils.reader;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuDataModelMapper;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import org.apache.ibatis.session.SqlSession;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 批量sqlite数据库弹幕读取类
 *
 * @author CuteLuoBo
 * @date 2023/3/9 16:10
 */
public class BatchSqliteDanMuReader {
    private List<SqliteDanMuReader> danMuReaderList;

    /**
     * 起始时间缓存map
     */
    private Map<Long, SqliteDanMuReader> startTimeMap;
    /**
     * 结束时间缓存map
     */
    private Map<Long, SqliteDanMuReader> endTimeMap;

    public BatchSqliteDanMuReader(@NotNull List<SqliteDanMuReader> danMuReaderList) {
        this.danMuReaderList = danMuReaderList;
        reloadDataBaseTimeTemp();
    }

    /**
     * 更新数据库的缓存时间，用于筛选
     */
    public void reloadDataBaseTimeTemp() {
        startTimeMap = new HashMap<>(danMuReaderList.size());
        endTimeMap = new HashMap<>(danMuReaderList.size());
        danMuReaderList.forEach(reader ->{
            DanMuDataModel first = reader.getCreateTimeFirst();
            //有数据时
            if (first != null&& first.getCreateTime()!=null) {
                DanMuDataModel end = reader.getCreateTimeEnd();
                startTimeMap.put(first.getCreateTime(), reader);
                endTimeMap.put(end.getCreateTime(), reader);
            }
        });
    }

    /**
     * 读取指定筛选条件的所有数据列表
     *
     * @param danMuDataModelSelector  筛选条件
     * @return 查询结果
     */
    public List<DanMuData> readAll(DanMuDataModelSelector danMuDataModelSelector) {
        List<DanMuData> danMuDataList = new ArrayList<>(20);
        if (danMuDataModelSelector.getStartCreateTime() != null) {
            long startTime = danMuDataModelSelector.getStartCreateTime();
            startTimeMap.entrySet().stream()
                    .filter(entry -> entry.getKey() <= startTime)
                    .forEach(entry -> danMuDataList.addAll(entry.getValue().readAll(danMuDataModelSelector)));
        } else {
            danMuReaderList.forEach(r -> danMuDataList.addAll(r.readAll(danMuDataModelSelector)));
        }
        return danMuDataList;
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
        List<DanMuData> danMuDataList = new ArrayList<>(pageSize);
        if (danMuDataModelSelector.getStartCreateTime() != null) {
            long startTime = danMuDataModelSelector.getStartCreateTime();
            startTimeMap.entrySet().stream()
                    .filter(entry -> entry.getKey() <= startTime)
                    .forEach(entry -> danMuDataList.addAll(entry.getValue().readListByPage(danMuDataModelSelector,current,pageSize)));
        } else {
            danMuReaderList.forEach(r -> danMuDataList.addAll(r.readListByPage(danMuDataModelSelector,current,pageSize)));
        }
        return danMuDataList;
    }

    /**
     * 读取指定筛选条件的所有数据列表
     *
     * @param danMuDataModelSelector  筛选条件
     * @param current                 当前访问页数
     * @param interval                分隔时间
     * @return 查询结果
     */
    public List<DanMuData> readListByTime(DanMuDataModelSelector danMuDataModelSelector,int current,long interval) {
        List<DanMuData> danMuDataList = new ArrayList<>();
        if (danMuDataModelSelector.getStartCreateTime() != null) {
            long startTime = danMuDataModelSelector.getStartCreateTime();
            //TODO 时间逻辑不对，应该是开始时间-结束时间/间隔
            startTimeMap.entrySet().stream()
                    .filter(entry -> entry.getKey() <= startTime)
                    .forEach(entry -> danMuDataList.addAll(entry.getValue().readListByTime(danMuDataModelSelector,current,interval)));
        } else {
            danMuReaderList.forEach(r -> danMuDataList.addAll(r.readListByTime(danMuDataModelSelector,current,interval)));
        }
        return danMuDataList;
    }

    /**
     * 通过时间范围查询弹幕数据（仅包含ID及时间）
     * @param danMuDataModelSelector    时间筛选条件
     * @return 查询结果（仅包含ID及时间）
     */
    public List<DanMuDataModel> listTimeDataByTime(DanMuDataModelSelector danMuDataModelSelector) {
        List<DanMuDataModel> danMuDataModelList = new ArrayList<>();
        if (danMuDataModelSelector.getStartCreateTime() != null) {
            long startTime = danMuDataModelSelector.getStartCreateTime();
            startTimeMap.entrySet().stream()
                    .filter(entry -> entry.getKey() <= startTime)
                    .forEach(entry -> danMuDataModelList.addAll(entry.getValue().listTimeDataByTime(danMuDataModelSelector)));
        } else {
            danMuReaderList.forEach(r -> danMuDataModelList.addAll(r.listTimeDataByTime(danMuDataModelSelector)));
        }
        return danMuDataModelList;
    }

    /**
     * 查询符合id-创建时间的弹幕数据（使用两列对应，避免多数据库环境下ID重复的问题）
     * @param idMap     id-创建时间映射表
     * @return 查询结果
     */
    public List<DanMuData> listModelByIdAndCreateTimeMap(DanMuDataModelSelector danMuDataModelSelector,Map<Long, Long> idMap){
        List<DanMuData> danMuDataList = new ArrayList<>();
        if (danMuDataModelSelector.getStartCreateTime() != null) {
            long startTime = danMuDataModelSelector.getStartCreateTime();
            startTimeMap.entrySet().stream()
                    //筛选具有对应时间后数据的数据库
                    .filter(entry -> entry.getKey() <= startTime)
                    .forEach(entry -> danMuDataList.addAll(entry.getValue().listModelByIdAndCreateTimeMap(idMap)));
        } else {
            danMuReaderList.forEach(r -> danMuDataList.addAll(r.listModelByIdAndCreateTimeMap(idMap)));
        }
        return danMuDataList;
    }

    /**
     * 统计符合条件的结果数量
     *
     * @param danMuDataModelSelector  筛选条件
     * @return 查询结果
     */
    public long countNum(DanMuDataModelSelector danMuDataModelSelector) {
        if (danMuDataModelSelector.getStartCreateTime() != null) {
            long startTime = danMuDataModelSelector.getStartCreateTime();
            return startTimeMap.entrySet().stream()
                    .filter(entry -> entry.getKey() <= startTime)
                    .mapToLong(entry -> entry.getValue().countNum(danMuDataModelSelector)).sum();
        } else {
            return danMuReaderList.stream().mapToLong(r -> r.countNum(danMuDataModelSelector)).sum();
        }
    }
}
