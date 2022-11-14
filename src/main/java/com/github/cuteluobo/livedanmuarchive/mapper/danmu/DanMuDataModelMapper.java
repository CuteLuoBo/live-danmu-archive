package com.github.cuteluobo.livedanmuarchive.mapper.danmu;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DataPage;
import com.github.cuteluobo.livedanmuarchive.pojo.DataPageSelector;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 弹幕数据模型mapper
 *
 * @author CuteLuoBo
 * @date 2022/4/6 11:26
 */
@Mapper
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
     * 批量添加数据
     * @param danMuDataModelList 数据列表
     * @return 变更数量
     */
    int addList(List<DanMuDataModel> danMuDataModelList);

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
     * @param current                当前页数
     * @param pageSize               本页结果数量
     * @return 查询组装好的数据列表
     */
    List<DanMuDataModel> listPageInternal(@Param("danMuDataModelSelector") DanMuDataModelSelector danMuDataModelSelector,@Param("current") int current,@Param("pageSize") int pageSize);

    /**
     * 统计数量
     *
     * @param danMuDataModelSelector
     * @return
     */
    int countNum(@Param("danMuDataModelSelector") DanMuDataModelSelector danMuDataModelSelector);

    default DataPage<DanMuDataModel> listPage(DanMuDataModelSelector danMuDataModelSelector, int current, int pageSize) {
        DataPage<DanMuDataModel> danMuDataModelDataPage = new DataPage<>();
        int total = countNum(danMuDataModelSelector);
        danMuDataModelDataPage.setTotal(total);
        danMuDataModelDataPage.setPageSize(pageSize);
        danMuDataModelDataPage.setMaxPageNum(total / pageSize);
        danMuDataModelDataPage.setCurrent(current);
        danMuDataModelDataPage.setData(listPageInternal(danMuDataModelSelector,current,pageSize));
        return danMuDataModelDataPage;
    }
}
