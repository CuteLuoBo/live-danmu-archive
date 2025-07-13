package com.github.cuteluobo.livedanmuarchive.mapper.main;

import com.github.cuteluobo.livedanmuarchive.model.DanMuTaskPlanModel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 主数据库中弹幕任务mapper映射接口
 *
 * @author: CuteLuoBo
 * @date: 2025/7/8  12:33
 * @version: 1.0.0
 */
@Mapper
public interface DanMuTaskPlanMapper {

    /**
     * 插入弹幕任务数据
     *
     * @param danMuTaskPlanModel 弹幕任务对象
     * */
    int insert(DanMuTaskPlanModel danMuTaskPlanModel);

    /**
     * 更新弹幕任务数据
     *
     * @param danMuTaskPlanModel 弹幕任务对象
     */
    int updateByPrimaryKeySelective( @Param("danMuTaskPlanModel") DanMuTaskPlanModel danMuTaskPlanModel);

    /**
     * 根据视频ID查询弹幕任务数据
     *
     * @param videoId 视频ID
     * @return 弹幕任务对象
     */
    DanMuTaskPlanModel selectOneByVideoId(String videoId);

    /**
     * 根据视频ID查询弹幕任务数据
     *
     * @param videoId 视频ID
     * @return 弹幕任务对象
     */
    DanMuTaskPlanModel selectOneByVideoIdAndNotFinish(String videoId);
}