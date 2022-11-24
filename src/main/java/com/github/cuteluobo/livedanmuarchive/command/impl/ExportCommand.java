package com.github.cuteluobo.livedanmuarchive.command.impl;

import com.github.cuteluobo.livedanmuarchive.command.base.AbstractCompositeCommand;
import com.github.cuteluobo.livedanmuarchive.command.base.action.SubCommandReg;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.manager.FileExportManager;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuExportDataInfo;
import com.github.cuteluobo.livedanmuarchive.service.DanMuFormatExportService;
import com.github.cuteluobo.livedanmuarchive.service.Impl.persistence.Sqlite2AssFileDanMuFormatExportServiceImpl;
import com.github.cuteluobo.livedanmuarchive.service.Impl.persistence.Sqlite2BiliBasFormatExportServiceImpl;
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
public class ExportCommand extends AbstractCompositeCommand {
    Logger logger = LoggerFactory.getLogger(ExportCommand.class);

    public ExportCommand() {
        super("export", new String[]{"exp"}, "导出弹幕数据,可使用exp help获取帮助");
    }

    @SubCommandReg(subCommandName = {"","help"})
    public boolean help(String... args) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("-----指定导出 -> 导出指定主播的指定时间弹幕\r\n")
                .append("export assign/as <ass/bas> <主播储存名> <视频开播时间> (匹配弹幕结束时间，为空时视为当前时间) (单P视频时间，为空时视为一整段时间)\n")
                .append("完整示例A：export assign ass 主播A 2022-11-14T11:30 2022-11-14T23:30 1:00:00\n")
                .append("简化示例A：exp as ass 主播A 2022-11-14T11:30 1:00:00\n")
                .append("简化示例B：exp as bas 主播A 2022-11-14T11:30 \n")
        ;
        System.out.println(stringBuilder);
        logger.debug(stringBuilder.toString());
        return true;
    }

    /**
     * BAS指定导出
     * @param args 指令系统传入的后续参数
     * @return 指令是否执行成功
     * @throws ServiceException 读取的SQL列表错误
     * @throws IOException 写入文件时出现错误
     */
    @SubCommandReg(subCommandName = {"assign","as"})
    public boolean exportAssign(String... args) throws ServiceException, IOException {
        logger.info("\r\n=====指定弹幕导出任务=====");
        long commandStartTime = System.currentTimeMillis();
        //最小指令要求数
        int mixNeedArgsNum = 3;
        if (args.length < mixNeedArgsNum) {
            System.out.println("指令参数数量不符合规则");
            logger.info("\n指令参数数量不符合规则");
            return false;
        }
        List<String> allModel = List.of(new String[]{"ass", "bas"});
        //解析指令
        String modelName = args[0];
        if (allModel.stream().noneMatch(s -> s.equalsIgnoreCase(modelName))) {
            logger.error("\n没有符合的导出模式,当前允许:{}",allModel);
            return false;
        }
        String liveName = args[1];
        //起始时间
        String startTimeString = args[2];
        LocalDateTime startTime;
        try {
            startTime = LocalDateTime.parse(startTimeString);
        } catch (DateTimeException dateTimeException) {
            logger.error("\n传入的起始时间字符串不符合规则(yyyy-MM-ddTHH-mm-ss)",dateTimeException);
            return false;
        }
        //尝试解析结束时间和分段时间
        LocalDateTime endTime = null;
        long partTime = 0;
        if (args.length > 3) {
            String arg3 = args[3];
            //首先尝试解析为结束时间
            try {
                endTime = LocalDateTime.parse(arg3);
            } catch (DateTimeException dateTimeException) {
                //后续还有参数时，抛出错误
                if (args.length > 4) {
                    logger.error("\n传入的结束时间字符串不符合规则(yyyy-MM-ddTHH-mm-ss)", dateTimeException);
                    return false;
                }
                //否则解析为分段时间
                else {
                    try {
                        partTime = FormatUtil.videoTimeString2MillTime(arg3);
                    } catch (NumberFormatException numberFormatException) {
                        logger.error("\n传入的分段时间不符合规则(hh:mm:ss)", numberFormatException);
                        return false;
                    }
                }
            }
        }
        if (args.length > 4) {
            try {
                partTime = FormatUtil.videoTimeString2MillTime(args[4]);
            } catch (NumberFormatException numberFormatException) {
                logger.error("\n传入的分段时间不符合规则(hh:mm:ss)", numberFormatException);
                return false;
            }
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        //开始结束时间不符时，自动调换位置
        if (startTime.compareTo(endTime) > 0) {
            LocalDateTime temp = startTime;
            startTime = endTime;
            endTime = temp;
            logger.info("\n开始时间不能大于结束时间，已自动调换位置。起始时间："+startTime+",结束时间："+endTime);
        }
        //检查目录和加载DB文件
        File danMuDir;
        List<File> dbFileList;
        try {
            danMuDir = getLiveDanMuDir(liveName);
            dbFileList = checkDbFileList(danMuDir);
        } catch (ServiceException e) {
            logger.info("\n{}", e.getLocalizedMessage());
            return false;
        }

        logger.info("\r\n读取到的数据库文件列表：\r\n{}\r\n",dbFileList.stream().map(File::getAbsolutePath).collect(Collectors.joining(",\r\n")));
        //创建以export-导出时间命名的储存路径
        File saveAssFilePath = new File(danMuDir.getAbsolutePath() + File.separator + "export-" + modelName + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")));
        //创建文件夹
        saveAssFilePath.mkdirs();
        DanMuFormatExportService<DanMuExportDataInfo<File>> service;
        switch (modelName) {
            case "ass":service = new Sqlite2AssFileDanMuFormatExportServiceImpl(liveName,dbFileList, saveAssFilePath);break;
            case "bas":service = new Sqlite2BiliBasFormatExportServiceImpl(dbFileList, liveName, saveAssFilePath, ".bas");break;
            default:logger.error("无法找到对应导出格式");
                return false;
        }

        long startTempStamp = startTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
        long endTempStamp = endTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
        //有分P
        List<DanMuExportDataInfo<File>> danMuExportDataInfoList = new ArrayList<>();
        //统计信息，总共符合条件的弹幕数量和实际使用的数量
        long totalDanMuNum = 0;
        long usageDanMuNum = 0;
        //分段时，使用切片读取
        if (partTime != 0) {
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

        logger.info("\r\n{}"
                , createOutputInfo(
                        dbFileList.size(), totalDanMuNum, usageDanMuNum
                        , danMuExportDataInfoList.size(), saveAssFilePath.getAbsolutePath(), commandStartTime
                )
        );
        return true;
    }

    private String createOutputInfo(int dbNum,long totalDanMuNum,long usageDanMuNum,int outFileNum,String path,long startTime) {
        return "---任务完成---" + "\r\n" +
                "尝试读取的数据库数量:" + dbNum + "\r\n" +
                "符合条件的弹幕数量:" + totalDanMuNum + "\r\n" +
                "导出弹幕数量:" + usageDanMuNum + "\r\n" +
                "总生成文件数量:" + outFileNum + "\r\n" +
                "生成文件路径:" + path + "\r\n" +
                "耗时:" + (System.currentTimeMillis() - startTime) + "ms" + "\r\n";
    }

    private File getLiveDanMuDir(String saveDirName) throws ServiceException  {
        //根据程序默认储存目录获取指定主播
        FileExportManager fileExportManager = FileExportManager.getInstance();
        File exportDir = fileExportManager.getExportDir();
        File[] matchDirs = exportDir.listFiles((File dir, String name) -> dir.isDirectory() && name.equals(saveDirName));
        if (matchDirs == null || matchDirs.length == 0) {
            throw new ServiceException("未找到符合主播名称 " + saveDirName + " 的弹幕保存文件夹，请确认此文件夹中含有数据：" + exportDir.getAbsolutePath());
        }
        //匹配的主播文件夹
        File matchDir = matchDirs[0];
        //获取内部文件列表
        File[] tempFileList = matchDir.listFiles();
        if (tempFileList == null || tempFileList.length == 0) {
            throw new ServiceException(saveDirName + "存档文件夹内部文件为空，请确认此文件夹中含有数据:" + matchDir.getAbsolutePath());
        }
        //读取主播的内部弹幕文件夹
        String normalSaveDirName = "danmu";
        File danMuDir = new File(matchDir.getAbsolutePath() + File.separator + normalSaveDirName);
        if (!danMuDir.exists()) {
            throw new ServiceException(saveDirName+"弹幕存档文件夹不存在，尝试获取的文件夹："+danMuDir.getAbsolutePath());
        }
        return danMuDir;
    }

    private List<File> checkDbFileList(File danMuDir) throws ServiceException {
        List<File> dbFileList = new ArrayList<>();
        File[] tempFileList = danMuDir.listFiles();
        if (tempFileList == null || tempFileList.length == 0) {
            throw new ServiceException("弹幕存档文件夹中没有文件，请确认此文件夹中含有数据:" + danMuDir.getAbsolutePath());
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
        return dbFileList;
    }
}
