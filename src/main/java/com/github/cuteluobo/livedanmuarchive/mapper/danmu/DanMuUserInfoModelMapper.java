package com.github.cuteluobo.livedanmuarchive.mapper.danmu;

import com.github.cuteluobo.livedanmuarchive.model.DanMuUserInfoModel;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author CuteLuoBo
 * @date 2022/4/4 14:51
 */
@Mapper
public interface DanMuUserInfoModelMapper {

    /**
     * 获取用户信息
     * @param id id
     * @return 查询结果
     */
    @Select("SELECT * FROM user_info WHERE id = #{id}")
    DanMuUserInfoModel getOneById(Integer id);

    /**
     * 获取用户信息
     * @param nickName 昵称
     * @return 查询结果
     */
    @Select("SELECT * FROM user_info WHERE nick_name = #{nickName}")
    DanMuUserInfoModel getOneByNickName(String nickName);

    /**
     * 添加用户信息
     * @param danMuUserInfoModel
     * @return 变更数量
     */
    @Insert("INSERT INTO user_info(nick_name,add_time) VALUES(#{nickName},#{addTime}) ")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addOne(DanMuUserInfoModel danMuUserInfoModel);

    /**
     * 添加用户信息
     * @param danMuUserInfoModelList
     * @return 变更数量
     */
    @Insert("<script>" +
            "INSERT INTO user_info(nick_name,add_time) VALUES" +
            "<foreach collection='danMuUserInfoModelList' item ='item' separator =','> " +
            "(#{item.nickName},#{item.addTime}) " +
            "</foreach>" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addList(@Param("list") List<DanMuUserInfoModel> danMuUserInfoModelList);
}
