package com.github.cuteluobo.livedanmuarchive.mapper;

import org.apache.ibatis.annotations.Select;

/**
 * 提供常用方法的表mapper
 * @author CuteLuoBo
 * @date 2023/3/14 11:56
 */
public interface BaseTableMapper {
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
     *  检验并创建所有表
     * @param skipCheck 跳过已有表检查
     */
    default void createAllTable(boolean skipCheck) {}
}
