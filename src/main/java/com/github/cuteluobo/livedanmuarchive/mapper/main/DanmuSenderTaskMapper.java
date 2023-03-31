package com.github.cuteluobo.livedanmuarchive.mapper.main;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuAccountTaskSelector;
import com.github.cuteluobo.livedanmuarchive.dto.DanMuSenderTaskSelector;
import com.github.cuteluobo.livedanmuarchive.model.DanmuAccountTaskModel;
import com.github.cuteluobo.livedanmuarchive.model.DanmuSenderTaskModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 弹幕发送主任务mapper
 *
 * @author CuteLuoBo
 * @date 2023/3/15 10:38
 */
@Mapper
public interface DanmuSenderTaskMapper {
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
    int insert(DanmuSenderTaskModel record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(DanmuSenderTaskModel record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    DanmuSenderTaskModel selectByPrimaryKey(Integer id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(DanmuSenderTaskModel record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(DanmuSenderTaskModel record);

    /**
     * 获得全部结果
     * @return 查询结果
     */
    @Select("SELECT * FROM danmu_sender_task")
    List<DanmuSenderTaskModel> selectAll();

    /**
     * 根据flag筛选列表
     * @param skip  是否跳过
     * @param taskFail  是否失败
     * @param limit 限制数量
     * @return 查询结果
     */
    @Select("<script>" +
            "SELECT * FROM danmu_sender_task " +
            "WHERE skip = #{skip,jdbcType=BOOLEAN} " +
            "AND task_fail = #{taskFail,jdbcType=BOOLEAN} " +
            "<choose>" +
            "<when test='finish' >" +
            "AND finish_time IS NOT NULL " +
            "</when>" +
            "<otherwise>AND finish_time IS NULL </otherwise>" +
            "</choose>" +
            "ORDER BY create_time DESC LIMIT #{limit}" +
            "</script>")
    List<DanmuSenderTaskModel> selectListByFlag(@Param("skip")boolean skip,@Param("taskFail")boolean taskFail,@Param("finish") boolean finish,@Param("limit") int limit);


    /**
     * 获得创建者UID相关的列表
     * @param creatorUid 视频创建者UID
     * @return 查询结果
     */
    @Select("SELECT * FROM danmu_sender_task where video_creator_uid = #{creatorUid}")
    List<DanmuSenderTaskModel> selectListByCreatorUid(@Param("creatorUid") String creatorUid);

    /**
     * 获得视频创建时间最新的一个结果
     * @param creatorUid 视频创建者UID
     * @return 查询结果
     */
    @Select("SELECT * FROM danmu_sender_task where video_creator_uid = #{creatorUid} ORDER BY video_created_time DESC LIMIT 1")
    DanmuSenderTaskModel selectLatestOneByCreatorUid(@Param("creatorUid") String creatorUid);

    /**
     * 获得视频创建时间最新的一个结果
     * @param platform   平台
     * @param skip       是否跳过
     * @param taskFail       是否错误
     * @param creatorUid 视频创建者UID
     * @param videoId 视频ID
     * @return 查询结果
     */
    @Select("SELECT * FROM danmu_sender_task where video_creator_uid = #{creatorUid} " +
            "AND video_id = #{videoId} " +
            "AND platform = #{platform} " +
            "AND skip = #{skip,jdbcType=BOOLEAN} " +
            "AND task_fail = #{taskFail,jdbcType=BOOLEAN} " +
            "ORDER BY video_created_time DESC LIMIT 1")
    DanmuSenderTaskModel selectOneLatest(@Param("platform")String platform,@Param("skip")boolean skip,@Param("taskFail")boolean taskFail,@Param("creatorUid") String creatorUid,@Param("videoId") String videoId);

    /**
     * 通过筛选条件查询列表
     * @param danMuSenderTaskSelector 筛选条件
     * @return 查询结果
     */
    List<DanmuSenderTaskModel> selectListBySelector(@Param("selector") DanMuSenderTaskSelector danMuSenderTaskSelector);
}