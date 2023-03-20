package com.github.cuteluobo.livedanmuarchive.mapper.main;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuAccountTaskSelector;
import com.github.cuteluobo.livedanmuarchive.model.DanmuAccountTaskModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 查询未完成的单个结果
     * @param senderTaskId 主任务ID
     * @param senderUid    发送者UID
     * @param videoId      视频ID
     * @param stop         是否中止
     * @return 查询结果
     */
    @Select("SELECT * FROM danmu_account_task WHERE danmu_sender_task_id = #{senderTaskId} " +
            "AND sender_uid = #{senderUid} AND video_id = #{videoId} AND stop = #{stop,jdbcType=BOOLEAN} AND finish_time IS NULL")
    DanmuAccountTaskModel selectOneByNoFinish(@Param("senderTaskId") int senderTaskId, @Param("senderUid") String senderUid, @Param("videoId") String videoId, @Param("stop") Boolean stop);

    /**
     * 通过筛选条件查询列表
     * @param danMuAccountTaskSelector 筛选条件
     * @return 查询结果
     */
    List<DanmuAccountTaskModel> selectListBySelector(@Param("selector") DanMuAccountTaskSelector danMuAccountTaskSelector);
}