package com.github.cuteluobo.livedanmuarchive.mapper.main;

import com.github.cuteluobo.livedanmuarchive.enums.database.DanMuDatabaseConstant;
import com.github.cuteluobo.livedanmuarchive.enums.database.MainDatabaseConstant;
import com.github.cuteluobo.livedanmuarchive.mapper.BaseTableMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 主数据库mapper
 * @author CuteLuoBo
 * @date 2023/3/14 11:50
 */
@Mapper
public interface MainTableMapper extends BaseTableMapper {

    /**
     * 创建弹幕发送任务表
     * @return 创建数量
     */
    @Update("CREATE TABLE \"danmu_sender_task\" (\n" +
            "\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
            "\"platform\" TEXT NOT NULL,\n" +
            "\"video_creator_uid\" TEXT NOT NULL,\n" +
            "\"video_id\" TEXT NOT NULL,\n" +
            "\"video_created_time\" INTEGER(13) NOT NULL,\n" +
            "\"danmu_total_num\" INTEGER NOT NULL,\n" +
            "\"danmu_sent_num\" INTEGER NOT NULL,\n" +
            "\"page_size\" INTEGER NOT NULL,\n" +
            "\"skip\" INTEGER(1) NOT NULL DEFAULT 0,\n" +
            "\"fail\" INTEGER(1) NOT NULL DEFAULT 0,\n" +
            "\"create_time\" INTEGER(13) NOT NULL,\n" +
            "\"update_time\" INTEGER(13) NOT NULL,\n" +
            "\"finish_time\" INTEGER(13)\n" +
            ");\n" +
            "CREATE INDEX \"index_dst_video_id\" ON \"danmu_sender_task\" (\"video_id\" ASC);")
    int createDanmuSenderTaskTable();

    /**
     * 创建弹幕账户信息缓存表
     * @return 创建数量
     */
    @Update("CREATE TABLE \"danmu_account_task\" (\n" +
            "\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
            "\"danmu_sender_task_id\" INTEGER NOT NULL,\n" +
            "\"sender_uid\" TEXT NOT NULL,\n" +
            "\"video_id\" TEXT NOT NULL,\n" +
            "\"last_video_part_index\" INTEGER NOT NULL,\n" +
            "\"last_video_part_cid\" INTEGER NOT NULL,\n" +
            "\"page_index\" INTEGER NOT NULL,\n" +
            "\"page_size\" INTEGER NOT NULL,\n" +
            "\"last_danmu_index\" INTEGER NOT NULL,\n" +
            "\"stop\" INTEGER(1) NOT NULL,\n" +
            "\"create_time\" INTEGER(13) NOT NULL,\n" +
            "\"update_time\" INTEGER(13) NOT NULL,\n" +
            "\"finish_time\" INTEGER(13)\n" +
            ");\n" +
            "CREATE INDEX \"index_act_danmu_sender_task_id\" ON \"danmu_account_task\" (\"danmu_sender_task_id\" ASC);\n" +
            "CREATE INDEX \"index_act_video_id\" ON \"danmu_account_task\" (\"video_id\" ASC);")
    int createDanmuAccountTaskTable();

    /**
     *  检验并创建所有表
     * @param skipCheck 跳过已有表检查
     */
    @Override
    default void createAllTable(boolean skipCheck) {
        if (skipCheck) {
            createDanmuSenderTaskTable();
            createDanmuAccountTaskTable();
        }else {
            if (checkTableExistBySqlite(MainDatabaseConstant.TABLE_DANMU_SENDER_TASK.getValue()) == 0) {
                createDanmuSenderTaskTable();
            }
            if (checkTableExistBySqlite(MainDatabaseConstant.TABLE_DANMU_ACCOUNT_TASK.getValue()) == 0) {
                createDanmuAccountTaskTable();
            }
        }
    }
}
