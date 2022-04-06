package com.github.cuteluobo.livedanmuarchive.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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

    public File getExportDir() {
        return exportDir;
    }

    public void setExportDir(File exportDir) {
        this.exportDir = exportDir;
    }
}
