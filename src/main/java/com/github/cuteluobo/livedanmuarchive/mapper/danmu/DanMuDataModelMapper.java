package com.github.cuteluobo.livedanmuarchive.mapper.danmu;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DataPage;
import com.github.cuteluobo.livedanmuarchive.pojo.DataPageSelector;

import java.util.List;

/**
 * 弹幕数据模型mapper
 *
 * @author CuteLuoBo
 * @date 2022/4/6 11:26
 */
public interface DanMuDataModelMapper {


    /**
     * 根据ID查询
     * @param id id
     * @return 查询结果
     */
    DanMuDataModel getOneById(Long id);

    /**
     * 添加数据
     * @param danMuDataModel 数据(ID设定无效，使用数据库ID自增)
     * @return 变更数量
     */
    int addOne(DanMuDataModel danMuDataModel);

    /**
     * 筛选列表
     * @param danMuDataModelSelector 筛选条件
     * @return 查询结果
     */
    List<DanMuDataModel> listModelBySelector(DanMuDataModelSelector danMuDataModelSelector);

    /**
     * 分页查询page
     *
     * @param danMuDataModelSelector 筛选条件
     * @param dataPageSelector 分页条件
     * @return 查询组装好的数据列表
     */
    DataPage<DanMuDataModel> listPage(DanMuDataModelSelector danMuDataModelSelector, DataPageSelector dataPageSelector);
}
