package com.github.cuteluobo.livedanmuarchive.service;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DataPage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 弹幕数据提供者，一般用于数据转换输出
 * @author CuteLuoBo
 * @date 2022/11/10 14:58
 */
public interface DanmuDataProvideService {

    /**
     * 根据时间筛选获取数据列表
     * @param danMuDataModelSelector 数据筛选对象
     * @return 符合条件的弹幕数据
     */
    List<DanMuData> getDataList(DanMuDataModelSelector danMuDataModelSelector);

    /**
     * 分页获取弹幕数据列表
     * @param danMuDataModelSelector 数据筛选对象
     * @param current   当前页数
     * @param pageSize  单页数量
     * @return 符合条件的弹幕分页数据
     */
    DataPage<DanMuData> getPage(DanMuDataModelSelector danMuDataModelSelector,int current,int pageSize);


}
