package com.github.cuteluobo.livedanmuarchive.mapper.danmu;

import com.github.cuteluobo.livedanmuarchive.model.DanMuUserInfoModel;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

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
    DanMuUserInfoModel getOneById(int id);

    /**
     * 获取符合传入ID列表条件的数据
     * @param idSet ID列表
     * @return 查询数据
     */
    @Select("<script>" +
            "SELECT * FROM user_info WHERE id IN " +
            "<foreach collection='list' item ='item' start='(' end=')' separator =','> " +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<DanMuUserInfoModel> getListById(@Param("list") Set<Integer> idSet);

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

    /**
     * 获取全部结果
     * @return 查询结果
     */
    @Select("SELECT * FROM user_info")
    List<DanMuUserInfoModel> getAll();
}
