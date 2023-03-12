package com.github.cuteluobo.livedanmuarchive.manager;

import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author CuteLuoBo
 * @date 2022/2/13 20:09
 */
public class FileExportManager {
    Logger logger = LoggerFactory.getLogger(FileExportManager.class);
    /**
     * 导出文件夹路径
     */
    private File exportDir = new File("export/");

    private static FileExportManager fileExportService = null;

    /**
     * 获取实例
     * @param exportDir 自定义的导出路径
     * @return 文件导出服务实例
     */
    public static FileExportManager getInstance(File exportDir) {
        if (fileExportService == null) {
            fileExportService = new FileExportManager(exportDir);
        }
        return fileExportService;
    }

    public static FileExportManager getInstance() {
        return getInstance(null);
    }

    private FileExportManager(File exportDir) {
        if (exportDir != null) {
            this.exportDir = exportDir;
        }
        checkPath();
    }

    /**
     * 检查并新建导出文件夹
     */
    private void checkPath(){
        if (!exportDir.exists()) {
            logger.info("导出目录不存在，尝试创建");
            logger.debug("创建目录：{}",exportDir.getAbsolutePath());
        }
        try {
            if (exportDir.mkdirs()) {
                logger.info("创建导出目录成功，路径：{}",exportDir.getAbsolutePath());
            }
        } catch (SecurityException securityException) {
            logger.error("尝试创建目录失败，请检查目录权限或输入路径是否正确，路径：{}",exportDir.getAbsolutePath());
        }
    }

    /**
     * 根据名称获取对应保存主播的弹幕保存文件夹
     * @param saveDirName 保存的文件名
     * @return 查找结果
     * @throws FileNotFoundException 无法找到主播文件夹时抛出错误
     */
    public File getLiveDanMuDir(String saveDirName) throws FileNotFoundException {
        //根据程序默认储存目录获取指定主播
        File exportDir = getExportDir();
        File[] matchDirs = exportDir.listFiles((File dir, String name) -> dir.isDirectory() && name.equals(saveDirName));
        if (matchDirs == null || matchDirs.length == 0) {
            throw new FileNotFoundException("未找到符合主播名称 " + saveDirName + " 的弹幕保存文件夹，请确认此文件夹中含有数据：" + exportDir.getAbsolutePath());
        }
        //匹配的主播文件夹
        File matchDir = matchDirs[0];
        //获取内部文件列表
        File[] tempFileList = matchDir.listFiles();
        if (tempFileList == null || tempFileList.length == 0) {
            throw new FileNotFoundException(saveDirName + "存档文件夹内部文件为空，请确认此文件夹中含有数据:" + matchDir.getAbsolutePath());
        }
        //读取主播的内部弹幕文件夹
        String normalSaveDirName = "danmu";
        File danMuDir = new File(matchDir.getAbsolutePath() + File.separator + normalSaveDirName);
        if (!danMuDir.exists()) {
            throw new FileNotFoundException(saveDirName+"弹幕存档文件夹不存在，尝试获取的文件夹："+danMuDir.getAbsolutePath());
        }
        return danMuDir;
    }

    /**
     * 从指定文件夹中，获取DB文件列表
     * @param danMuDir  待查询的文件夹
     * @return DB文件列表
     * @throws FileNotFoundException 文件夹中没有文件时，抛出错误
     */
    public List<File> checkDbFileList(File danMuDir) throws FileNotFoundException {
        List<File> dbFileList = new ArrayList<>();
        File[] tempFileList = danMuDir.listFiles();
        if (tempFileList == null || tempFileList.length == 0) {
            throw new FileNotFoundException("弹幕存档文件夹中没有文件，请确认此文件夹中含有数据:" + danMuDir.getAbsolutePath());
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

    public File getExportDir() {
        return exportDir;
    }

    public void setExportDir(File exportDir) {
        this.exportDir = exportDir;
    }
}
