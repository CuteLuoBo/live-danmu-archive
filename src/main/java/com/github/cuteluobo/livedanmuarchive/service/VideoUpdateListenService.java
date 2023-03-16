package com.github.cuteluobo.livedanmuarchive.service;

/**
 * 视频更新监听服务接口
 * 此类应通过具体实现获取到更新的视频，将更新的数据储存到数据库中，由具体实现类根据数据库数据运行或更新
 * @author CuteLuoBo
 * @date 2023/3/16 10:52
 */
public interface VideoUpdateListenService {
    /**
     * 开启针对某个特定用户的（动态）视频更新信息监听
     * @param userId  用户ID
     * @return 是否开启成功
     */
    boolean startVideoUpdateListen(String userId);

    /**
     * 手动添加视频的ID信息，一般由用户通过直接命令添加
     * @param videoId 视频ID
     * @return 是否添加成功
     */
    boolean addVideo(String videoId);
}
