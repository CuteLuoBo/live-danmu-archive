package com.github.cuteluobo.livedanmuarchive.mapper.main;

import com.github.cuteluobo.livedanmuarchive.model.DanmuAccountTaskModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 弹幕账户任务mapper
 *
 * @author CuteLuoBo
 * @date 2023/3/15 10:38
 */
@Mapper
public interface DanmuAccountTaskMapper {
    /**
     * delete by primary key
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * insert record to table
     * @param record the record
     * @return insert count
     */
    int insert(DanmuAccountTaskModel record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(DanmuAccountTaskModel record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    DanmuAccountTaskModel selectByPrimaryKey(Integer id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(DanmuAccountTaskModel record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(DanmuAccountTaskModel record);
}