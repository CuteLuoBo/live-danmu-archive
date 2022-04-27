package com.github.cuteluobo.livedanmuarchive.service;

import com.github.cuteluobo.livedanmuarchive.enums.ExportPattern;
import com.github.cuteluobo.livedanmuarchive.manager.FileExportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * 使用文件系统的弹幕导出抽象类，封装基础文件操作供子类使用
 * @author CuteLuoBo
 * @date 2022/4/3 18:10
 */
public abstract class AbstractFilesDanMuExportService implements DanMuExportService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 文件分类方法
     */
    ExportPattern danMuExportPattern;

    /**
     * 文件名前缀
     */
    private String fileNamePrefix = "";

    /**
     * 文件名后缀
     */
    private String fileNameSuffix = "";
    /**
     * 文件时间命名格式
     */
    private String fileNameTimeFormat = "yyyy-MM-dd HH-mm-ss";

    /**
     * 文件日期命名格式
     */
    private String fileDataFormat = "yyyy-MM-dd";

    /**
     * 日期格式化
     */
    private DateTimeFormatter normalDateFormatter = DateTimeFormatter.ofPattern(fileDataFormat);

    /**
     * 时间格式化
     */
    private DateTimeFormatter normalDateTimeFormatter = DateTimeFormatter.ofPattern(fileNameTimeFormat);

    /**
     * 直播者名称
     */
    private String liveName = "unknown";

    /**
     * 默认保存文件夹名称
     */
    public static final String SAVE_DIR_NAME = "danmu";

    /**
     * 保存文件夹路径
     */
    private File savePath ;

    /**
     * 保存文件夹路径字符串
     */
    private String savePathString;

    private String saveFileName;

    public AbstractFilesDanMuExportService(String liveName, ExportPattern danMuExportPattern) throws IOException {
        if (liveName != null && liveName.trim().length() != 0) {
            //过滤可能导致文件系统错误的路径符
            this.liveName = liveName.replace("\\"," ").replace("/"," ");
        }
        this.danMuExportPattern = danMuExportPattern;
        initSavePath();
    }

    public AbstractFilesDanMuExportService(String fileNameFormat, String liveName, ExportPattern danMuExportPattern) throws IOException {
        if (liveName != null && liveName.trim().length() != 0) {
            this.liveName = liveName;
        }
        if (fileNameFormat != null && fileNameFormat.trim().length() != 0) {
            this.fileNameTimeFormat = fileNameFormat;
        }
        this.danMuExportPattern = danMuExportPattern;
        initSavePath();
    }

    /**
     * 初始化保存文件夹路径
     * @throws IOException
     */
    protected void initSavePath() throws IOException {
        //组装保存路径
        FileExportManager fileExportManager = FileExportManager.getInstance();
        StringBuilder sb = new StringBuilder(fileExportManager.getExportDir().getCanonicalFile().toString());
        sb.append(File.separator).append(liveName).append(File.separator);
        sb.append(SAVE_DIR_NAME).append(File.separator);
        savePath = new File(sb.toString());
        savePathString = sb.toString();
        //检查文件夹
        if (!savePath.exists()) {
            logger.info("弹幕导出目录不存在，尝试创建");
            try {
                if (savePath.mkdirs()) {
                    logger.info("创建导出目录成功，路径：{}",savePath.getAbsolutePath());
                }
            } catch (SecurityException securityException) {
                logger.error("尝试创建目录失败，请检查目录权限或输入路径是否正确，路径：{}",savePath.getAbsolutePath());
            }
        }
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public void setFileNamePrefix(String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    public String getFileNameSuffix() {
        return fileNameSuffix;
    }

    public void setFileNameSuffix(String fileNameSuffix) {
        this.fileNameSuffix = fileNameSuffix;
    }

    public String getFileNameTimeFormat() {
        return fileNameTimeFormat;
    }

    public void setFileNameTimeFormat(String fileNameTimeFormat) {
        this.fileNameTimeFormat = fileNameTimeFormat;
    }

    public DateTimeFormatter getNormalDateTimeFormatter() {
        return normalDateTimeFormatter;
    }

    public void setNormalDateTimeFormatter(DateTimeFormatter normalDateTimeFormatter) {
        this.normalDateTimeFormatter = normalDateTimeFormatter;
    }

    public ExportPattern getDanMuExportPattern() {
        return danMuExportPattern;
    }

    public void setDanMuExportPattern(ExportPattern danMuExportPattern) {
        this.danMuExportPattern = danMuExportPattern;
    }

    public String getLiveName() {
        return liveName;
    }

    public void setLiveName(String liveName) {
        this.liveName = liveName;
    }

    public File getSavePath() {
        return savePath;
    }

    public void setSavePath(File savePath) {
        this.savePath = savePath;
    }

    public String getSavePathString() {
        return savePathString;
    }

    public void setSavePathString(String savePathString) {
        this.savePathString = savePathString;
    }

    public String getFileDataFormat() {
        return fileDataFormat;
    }

    public void setFileDataFormat(String fileDataFormat) {
        this.fileDataFormat = fileDataFormat;
    }

    public DateTimeFormatter getNormalDateFormatter() {
        return normalDateFormatter;
    }

    public void setNormalDateFormatter(DateTimeFormatter normalDateFormatter) {
        this.normalDateFormatter = normalDateFormatter;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public void setSaveFileName(String saveFileName) {
        this.saveFileName = saveFileName;
    }
}
