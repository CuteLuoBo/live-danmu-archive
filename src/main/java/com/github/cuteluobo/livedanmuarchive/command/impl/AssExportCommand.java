package com.github.cuteluobo.livedanmuarchive.command.impl;

import com.github.cuteluobo.livedanmuarchive.command.base.AbstractCompositeCommand;
import com.github.cuteluobo.livedanmuarchive.command.base.action.SubCommandReg;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.manager.FileExportManager;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuExportDataInfo;
import com.github.cuteluobo.livedanmuarchive.service.Impl.persistence.Sqlite2AssFileDanMuFormatExportServiceImpl;
import com.github.cuteluobo.livedanmuarchive.utils.FormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
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
        stringBuilder.append("指定导出 -> 导出指定主播的指定时间弹幕\r\n")
                .append("assExport(aexp) assign(as) <主播名/配置ID> <视频开播时间> <匹配弹幕结束时间> (单P视频时间)\n")
                .append("示例A：assExport assign 主播A 2022-11-14T11:30 2022-11-14T23:30 1:00:00\n")
                .append("示例B：aexp as 主播A 2022-11-14T11:30 2022-11-14T23:30\n")
        ;
        System.out.println(stringBuilder);
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
    @SubCommandReg(subCommandName = {"assign","as"})
    public boolean exportAssign(String... args) throws ServiceException, IOException {
        logger.info("\r\n=====ASS弹幕导出任务=====");
        long commandStartTime = System.currentTimeMillis();
        //最小指令要求数
        int mixNeedArgsNum = 3;
        if (args.length < mixNeedArgsNum) {
            System.out.println("指令参数数量不符合规则");
            logger.info("\n指令参数数量不符合规则");
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
            logger.error("\n传入的时间字符串不符合规则",dateTimeException);
            return false;
        }
        //开始结束时间不符时，自动调换位置
        if (startTime.compareTo(endTime) > 0) {
            LocalDateTime temp = startTime;
            startTime = endTime;
            endTime = temp;
            logger.info("\n开始时间不能大于结束时间，已自动调换位置。起始时间："+startTime+",结束时间："+endTime);
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
            logger.info("\n未找到符合主播名称 "+liveName+" 的弹幕保存文件夹");
            return false;
        }
        //匹配的主播文件夹
        File matchDir = matchDirs[0];
        List<File> dbFileList = new ArrayList<>();
        //获取内部文件列表
        File[] tempFileList = matchDir.listFiles();
        if (tempFileList == null || tempFileList.length == 0) {
            logger.info("\n"+liveName+"存档文件夹内部文件为空");
            return false;
        }
        //读取主播的内部弹幕文件夹
        String normalSaveDirName = "danmu";
        File danMuDir = new File(matchDir.getAbsolutePath() + File.separator + normalSaveDirName);
        if (!danMuDir.exists()) {
            logger.info("\n"+liveName+"弹幕存档文件夹不存在");
            return false;
        }
        tempFileList = danMuDir.listFiles();
        if (tempFileList == null || tempFileList.length == 0) {
            logger.info("\n"+liveName+"弹幕存档文件夹中没有文件");
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
                if (dbFilesArray != null && dbFilesArray.length > 0) {
                    //过滤为文件且转list
                    dbFileList.addAll(Arrays.stream(dbFilesArray).filter(File::isFile).collect(Collectors.toList()));
                }
            } else if (f.getName().endsWith(dbFileSuffix)) {
                dbFileList.add(f);
            }
        }

        logger.info("\r\n读取到的数据库文件列表：\r\n{}\r\n",dbFileList.stream().map(File::getAbsolutePath).collect(Collectors.joining(",\r\n")));
        //创建以export-导出时间命名的储存路径
        File saveAssFilePath = new File(danMuDir.getAbsolutePath() + File.separator + "export-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")));
        //创建文件夹
        saveAssFilePath.mkdirs();
        Sqlite2AssFileDanMuFormatExportServiceImpl service = new Sqlite2AssFileDanMuFormatExportServiceImpl(liveName, dbFileList, saveAssFilePath);
        long startTempStamp = startTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
        long endTempStamp = endTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
        //有分P
        List<DanMuExportDataInfo<File>> danMuExportDataInfoList = new ArrayList<>();
        //统计信息，总共符合条件的弹幕数量和实际使用的数量
        long totalDanMuNum = 0;
        long usageDanMuNum = 0;
        if (partTimeString != null) {
            long partTime = FormatUtil.videoTimeString2MillTime(partTimeString);
            //自动分P并创建
            do {
                DanMuExportDataInfo<File> danMuExportDataInfo = service.formatExportBySelector(FormatUtil.millTime2localDataTime(startTempStamp), FormatUtil.millTime2localDataTime(startTempStamp + partTime));
                danMuExportDataInfoList.add(danMuExportDataInfo);
                totalDanMuNum += danMuExportDataInfo.getTotalNum();
                usageDanMuNum += danMuExportDataInfo.getTotalNum();
                startTempStamp += partTime;
            } while (startTempStamp< endTempStamp);
        } else {
            DanMuExportDataInfo<File> danMuExportDataInfo = service.formatExportBySelector(startTime, endTime);
            danMuExportDataInfoList.add(danMuExportDataInfo);
            totalDanMuNum += danMuExportDataInfo.getTotalNum();
            usageDanMuNum += danMuExportDataInfo.getTotalNum();
        }
        StringBuilder outputStringBuilder = new StringBuilder();
        outputStringBuilder.append("---任务完成---").append("\r\n")
                .append("尝试读取的数据库数量:").append(dbFileList.size()).append("\r\n")
                .append("符合条件的弹幕数量:").append(totalDanMuNum).append("\r\n")
                .append("导出弹幕数量:").append(usageDanMuNum).append("\r\n")
                .append("总生成文件数量:").append(danMuExportDataInfoList.size()).append("\r\n")
                .append("生成文件路径:").append(saveAssFilePath.getAbsolutePath()).append("\r\n")
                .append("耗时:").append(System.currentTimeMillis()-commandStartTime).append("ms").append("\r\n")
        ;
        logger.info("\r\n{}",outputStringBuilder);
        return true;
    }
}
