package com.github.cuteluobo.livedanmuarchive.mapper.danmu;

import com.github.cuteluobo.livedanmuarchive.model.DanMuFormatModel;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuFormat;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 弹幕样式模型mapper
 * @author CuteLuoBo
 * @date 2022/4/6 10:48
 */
@Mapper
public interface DanMuFormatModelMapper {

    /**
     * 根据ID获取
     *
     * @param id id
     * @return 结果
     */

    @Results(id = "danMuFormatModel", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "fontColor", column = "font_color"),
            @Result(property = "fontSize", column = "font_size"),
            @Result(property = "transitionType", column = "transition_type"),
            @Result(property = "popupStyle", column = "popup_style")
    })
    @Select("SELECT id,font_color,font_size,transition_type,popup_style FROM danmu_format WHERE id = #{id}")
    DanMuFormatModel getOneById(Long id);

    /**
     * 新增
     * @param danMuFormatModel
     * @return 变更数量
     */
    @Insert("INSERT INTO danmu_format(font_color,font_size,transition_type,popup_style) " +
            "VALUES(#{fontColor},#{fontSize},#{transitionType},#{popupStyle})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addOne(DanMuFormatModel danMuFormatModel);


    /**
     * 根据传入模型筛选列表
     * WARN 能否正常返回？
     * @param danMuFormatModel 查询的模型
     * @return 查询列表
     */
    @ResultMap("danMuFormatModel")
    List<DanMuFormatModel> getListByModel(DanMuFormatModel danMuFormatModel);

}
