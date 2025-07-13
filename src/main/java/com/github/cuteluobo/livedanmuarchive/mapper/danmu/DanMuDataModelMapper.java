package com.github.cuteluobo.livedanmuarchive.mapper.danmu;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DataPage;
import com.github.cuteluobo.livedanmuarchive.pojo.DataPageSelector;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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
     * 根据创建时间返回降序/升序的一个结果
     * @param desc 是否为降序
     * @return 查询结果
     */
    DanMuDataModel getOneByCreateTimeOrder(boolean desc);

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
    List<DanMuDataModel> listModelBySelector(@Param("danMuDataModelSelector") DanMuDataModelSelector danMuDataModelSelector);


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
     * 通过窗口函数，根据间隔时间分行读取弹幕数据
     * @param danMuDataModelSelector    筛选条件
     * @param current                   当前页数
     * @param interval                  间隔时间，单位毫秒(建议20000)
     * @return 查询结果
     */
    List<DanMuDataModel> listPageByTime(@Param("danMuDataModelSelector") DanMuDataModelSelector danMuDataModelSelector,@Param("current") int current,@Param("interval") long interval);
    /**
     * 统计数量
     *
     * @param danMuDataModelSelector
     * @return
     */
    int countNum(@Param("danMuDataModelSelector") DanMuDataModelSelector danMuDataModelSelector);

    /**
     * 查询符合id-创建时间的弹幕数据
     * @param idMap     id-创建时间映射表
     * @return 查询结果
     */
    List<DanMuDataModel> listModelByIdAndCreateTimeMap(@Param("idMap")Map<Long, Long> idMap);
    /**
     * 通过时间范围查询弹幕数据（仅包含ID及时间）
     * @param danMuDataModelSelector    时间筛选条件
     * @return 查询结果（仅包含ID及时间）
     */
    List<DanMuDataModel> listTimeDataByTime(@Param("danMuDataModelSelector")DanMuDataModelSelector danMuDataModelSelector);

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
