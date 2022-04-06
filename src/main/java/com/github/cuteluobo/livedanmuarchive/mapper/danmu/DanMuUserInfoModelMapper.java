package com.github.cuteluobo.livedanmuarchive.mapper.danmu;

import com.github.cuteluobo.livedanmuarchive.model.DanMuUserInfoModel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

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
     * 获取用户信息
     * @param danMuUserInfoModel
     * @return 查询结果
     */
    @Insert("INSERT INTO user_info(nick_name,add_time) VALUES(#{nickName},#{addTime}) ")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addOne(DanMuUserInfoModel danMuUserInfoModel);
}
