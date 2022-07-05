package com.github.cuteluobo.livedanmuarchive.mapper.danmu;

import com.github.cuteluobo.livedanmuarchive.enums.DanMuDatabaseConstant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 弹幕数据库，表操作相关mapper
 * @author CuteLuoBo
 * @date 2022/4/5 15:40
 */
@Mapper
public interface DanMuDatabaseTableMapper {

    /**
     * 检查SQLITE表是否存在
     * @param tableName 表名
     * @return 检索数量
     */
    @Select("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND tbl_name = #{tableName} ")
    int checkTableExistBySqlite(String tableName);

    /**
     * 检查SQLITE表是否存在（备用方法）
     * 参考：https://stackoverflow.com/questions/1601151/how-do-i-check-in-sqlite-whether-a-table-exists
     * @param tableName 表名
     * @return 检索数量
     */
    @Select("PRAGMA table_info(#{tableName})")
    int checkTableExistBySqliteReserve(String tableName);

    /**
     * 创建弹幕用户信息表
     * @return 创建数量
     */
    @Update("CREATE TABLE 'user_info' (" +
            "'id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "'nick_name' TEXT(64) NOT NULL," +
            "'add_time' INTEGER(32) NOT NULL"+
            ")")
    int createUserInfoTable();

    /**
     * 创建弹幕信息表
     * @return 创建数量
     */
    @Update("CREATE TABLE 'danmu_data' (" +
            "'id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "'user_id' INTEGER(16) NOT NULL," +
            "'data' TEXT," +
            "'format' INTEGER(16) NOT NULL DEFAULT 0," +
            "'type' INTEGER(4) NOT NULL," +
            "'create_time' INTEGER(32) NOT NULL," +
            "'create_time_text' TEXT," +
            "CONSTRAINT 'fk_uid' FOREIGN KEY ('user_id') REFERENCES 'user_info' ('id')," +
            "CONSTRAINT 'fk_colorId' FOREIGN KEY ('format') REFERENCES 'danmu_format' ('id')" +
            ")")
    int createDanmuDataTable();

    /**
     * 创建弹幕信息表
     * @return 创建数量
     */
    @Update("CREATE TABLE 'danmu_format' (" +
            "'id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "'font_color' INTEGER(16) NOT NULL DEFAULT 0," +
            "'font_size' INTEGER(16) NOT NULL DEFAULT 4," +
            "'transition_type' INTEGER(8) NOT NULL," +
            "'popup_style' INTEGER(1) NOT NULL DEFAULT 0" +
            ")")
    int createDanmuFormatTable();

    default void test1(){
        System.out.println("rua");
    }

    default void test1(String value){
        System.out.println(value);
    }
}
