package com.github.cuteluobo.livedanmuarchive.command.impl;

import com.github.cuteluobo.livedanmuarchive.command.base.AbstractCompositeCommand;
import com.github.cuteluobo.livedanmuarchive.command.base.action.SubCommandReg;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.manager.FileExportManager;
import com.github.cuteluobo.livedanmuarchive.service.Impl.persistence.Sqlite2AssFileDanMuFormatExportServiceImpl;
import com.github.cuteluobo.livedanmuarchive.utils.FormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 弹幕导出指令
 * @author CuteLuoBo
 * @date 2022/11/14 22:49
 */
public class AssExportCommand extends AbstractCompositeCommand {
    Logger logger = LoggerFactory.getLogger(AssExportCommand.class);

    public AssExportCommand() {
        super("assExport", new String[]{"aexp"}, "导出弹幕数据为ASS文件,使用 assExport help获取帮助");
    }

    @SubCommandReg(subCommandName = {"","help"})
    public boolean help(String... args) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/assExport assign <主播名/配置ID> <视频开播时间> <匹配弹幕结束时间> (单P视频时间)")
                .append("示例：/assExport assign 主播A 2022-11-14T11:30 2022-11-14T23:30 1:00:00")
                .append("\r\n->导出指定主播的指定时间弹幕").append("\r\n")
        ;
        System.out.println(stringBuilder.toString());
        logger.debug(stringBuilder.toString());
        return true;
    }

    /**
     * 指定导出
     * @param args 指令系统传入的后续参数
     * @return 指令是否执行成功
     * @throws ServiceException 读取的SQL列表错误
     * @throws IOException 写入ASS文件时出现错误
     */
    @SubCommandReg(subCommandName = {"assign"})
    public boolean exportAssign(String... args) throws ServiceException, IOException {
        long commandStartTime = System.currentTimeMillis();
        //最小指令要求数
        int mixNeedArgsNum = 3;
        if (args.length < mixNeedArgsNum) {
            System.out.println("指令参数数量不符合规则");
            logger.info("指令参数数量不符合规则");
            return false;
        }
        //解析指令
        String liveName = args[0];
        String startTimeString = args[1];
        String endTimeString = args[2];
        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            startTime = LocalDateTime.parse(startTimeString);
            endTime = LocalDateTime.parse(endTimeString);
        } catch (DateTimeException dateTimeException) {
            System.err.println("传入的时间字符串不符合规则");
            logger.error("传入的时间字符串不符合规则",dateTimeException);
            return false;
        }
        //开始结束时间不符时，自动调换位置
        if (startTime.compareTo(endTime) > 0) {
            LocalDateTime temp = startTime;
            startTime = endTime;
            endTime = temp;
            System.out.println("开始时间不能大于结束时间，已自动调换位置。起始时间："+startTime+",结束时间："+endTime);
            logger.info("开始时间不能大于结束时间，已自动调换位置。起始时间："+startTime+",结束时间："+endTime);
        }
        String partTimeString = null;
        if (args.length > mixNeedArgsNum) {
            partTimeString = args[3];
        }
        //根据程序默认储存目录获取指定主播
        FileExportManager fileExportManager = FileExportManager.getInstance();
        File exportDir = fileExportManager.getExportDir();
        File[] matchDirs = exportDir.listFiles((File dir, String name) -> dir.isDirectory() && name.equals(liveName));
        if (matchDirs == null || matchDirs.length == 0) {
            System.out.println("未找到符合主播名称 "+liveName+" 的弹幕保存文件夹");
            logger.info("未找到符合主播名称 "+liveName+" 的弹幕保存文件夹");
            return false;
        }
        //匹配的主播文件夹
        File matchDir = matchDirs[0];
        List<File> dbFileList = new ArrayList<>();
        //获取内部文件列表
        File[] tempFileList = matchDir.listFiles((File dir, String name) -> true);
        if (tempFileList == null || tempFileList.length == 0) {
            System.out.println(liveName+"存档文件夹内部文件为空");
            logger.info(liveName+"存档文件夹内部文件为空");
            return false;
        }
        //数据库文件后缀
        String dbFileSuffix = ".db";
        //遍历获取文件夹内部
        for (File f :
                tempFileList) {
            //为目录时，向内部遍历一层
            if (f.isDirectory()) {
                //包含指定后缀
                File[] dbFilesArray = f.listFiles((File dir, String name) -> name.endsWith(dbFileSuffix));
                if (dbFilesArray != null && !dbFileList.isEmpty()) {
                    //过滤为文件且转list
                    dbFileList.addAll(Arrays.stream(dbFilesArray).filter(File::isFile).collect(Collectors.toList()));
                }
            } else if (f.getName().endsWith(dbFileSuffix)) {
                dbFileList.add(f);
            }
        }
        Sqlite2AssFileDanMuFormatExportServiceImpl service = new Sqlite2AssFileDanMuFormatExportServiceImpl(liveName, dbFileList);
        long startTempStamp = startTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
        long endTempStamp = endTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
        //有分P
        List<File> createAssFileList = new ArrayList<>();
        if (partTimeString != null) {
            long partTime = FormatUtil.videoTimeString2MillTime(partTimeString);
            int partNum = 0;
            //自动分P并创建
            while(true){
                createAssFileList.add(service.formatExportBySelector(FormatUtil.millTime2localDataTime(startTempStamp), FormatUtil.millTime2localDataTime(startTempStamp + partTime)));
                if (startTempStamp > endTempStamp) {
                    break;
                }
                partNum++;
                startTempStamp += partNum * partTime;
            }
        } else {
            createAssFileList.add(service.formatExportBySelector(startTime, endTime));
        }
        StringBuilder outputStringBuilder = new StringBuilder();
        //TODO 完成更多信息显示
        outputStringBuilder.append("ASS导出完成").append("\r\n")
                .append("尝试读取的数据库数量:").append(dbFileList.size()).append("\r\n")
                .append("符合条件的弹幕数量:").append("x").append("\r\n")
                .append("导出弹幕数量:").append("x").append("\r\n")
                .append("总生成文件数量:").append(createAssFileList.size()).append("\r\n")
                //TODO 解决默认会生成DB文件旁，对服务类增加一个统一保存路径的构建方法
                .append("生成文件路径").append(matchDir.getAbsolutePath()).append("\r\n")
                .append("耗时").append(System.currentTimeMillis()-commandStartTime).append("ms").append("\r\n")
        ;
        System.out.println(outputStringBuilder);
        logger.debug(outputStringBuilder.toString());
        return true;
    }
}
