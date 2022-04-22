package com.github.cuteluobo.livedanmuarchive.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cuteluobo.livedanmuarchive.enums.ExportPattern;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.service.AbstractFilesDanMuExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author CuteLuoBo
 * @date 2022/2/13 20:51
 */
public class JsonDanMuExportServiceImpl extends AbstractFilesDanMuExportService {
    Logger logger = LoggerFactory.getLogger(JsonDanMuExportServiceImpl.class);



    /**
     * 文件名后缀
     */
    public static final String FILE_SUFFIX = ".json";



    /**
     * 现在使用的文件
     */
    private File nowUseFile;


    private ObjectMapper objectMapper = new ObjectMapper();


    public JsonDanMuExportServiceImpl(String liveName, ExportPattern danMuExportPattern) throws IOException {
        super(liveName,danMuExportPattern);
    }

    /**
     *
     * @param fileNameFormat 文件名时间格式
     * @param liveName 直播间名称
     * @throws IOException
     */
    public JsonDanMuExportServiceImpl(String fileNameFormat, String liveName, ExportPattern danMuExportPattern) throws IOException {
        super(fileNameFormat,liveName, danMuExportPattern);
        initSavePath();
    }



    /**
     * 创建弹幕保存文件(读取文件不存在时)
     * @return 创建是否成功
     */
    private Boolean createDanMuSaveFile() {
        logger.debug("弹幕保存文件不存在或为空，尝试创建");
        //获取当前时间
        LocalDateTime nowTime = LocalDateTime.now();
        //设置默认转换时间格式
        DateTimeFormatter dateTimeFormatter = getNormalDateTimeFormatter();
        //解析自定义的转换模式
        try {
            dateTimeFormatter = DateTimeFormatter.ofPattern(getFileNameTimeFormat());
        } catch (IllegalArgumentException exception) {
            logger.error("传入的文件名时间命名规则 '{}' 无效，使用默认解析方式",getFileNameTimeFormat());
        }
        //拼接文件名
        String timeString = nowTime.format(dateTimeFormatter);
        StringBuilder fileNameStringBuilder = new StringBuilder();
        fileNameStringBuilder.append(getFileNamePrefix()).append(" ").append(timeString).append(" ").append(getFileNameSuffix()).append(FILE_SUFFIX);
        nowUseFile = new File(super.getSavePathString() + fileNameStringBuilder.toString());
        //检查文件
        if (!nowUseFile.exists()) {
            logger.info("弹幕保存文件不存在，尝试创建");
            try {
                if (nowUseFile.createNewFile()) {
                    logger.debug("创建保存文件成功，路径：{}",nowUseFile.getAbsolutePath());
                    return true;
                }else {
                    logger.info("弹幕保存文件创建失败，待重试");
                    return false;
                }
            } catch (SecurityException | IOException securityException) {
                logger.error("创建保存文件失败，请检查目录权限或输入路径是否正确，路径：{}",nowUseFile.getAbsolutePath(),securityException);
            }
        }
        return true;
    }

    /**
     * 单弹幕导出操作
     *
     * @param danMuData 弹幕信息
     * @return 是否导出成功
     */
    @Override
    public Boolean export(DanMuData danMuData) throws IOException {
        //循环创建，确保有可写入文件
        while (nowUseFile == null) {
            createDanMuSaveFile();
        }
//        if (saveJsonFileOutputStream == null ) {
//            initJsonWriter();
//        }
        //文件流追加输出
        try(Writer outputStreamWriter = new FileWriter(nowUseFile, true)){
            outputStreamWriter.write(objectMapper.writeValueAsString(danMuData));
            outputStreamWriter.write("\r\n");
            outputStreamWriter.flush();
            return true;
        }catch (IOException ioException){
            logger.error("数据导出时发生IO错误：",ioException);
        }
        return false;
    }

    /**
     * 批量导出
     *
     * @param danMuDataList 弹幕信息列表
     * @return 是否导出成功
     */
    @Override
    public Boolean batchExport(List<DanMuData> danMuDataList) {
        //循环创建，确保有可写入文件
        while (nowUseFile == null) {
            createDanMuSaveFile();
        }
        //文件流追加输出
        try(Writer outputStreamWriter = new FileWriter(nowUseFile, true)){
            for (DanMuData danMuData :
                    danMuDataList) {
                outputStreamWriter.write(objectMapper.writeValueAsString(danMuData));
                outputStreamWriter.write("\r\n");
            }
            outputStreamWriter.flush();
            return true;
        }catch (IOException ioException){
            logger.error("数据导出时发生IO错误：",ioException);
        }
        return false;
    }

    private Boolean saveToFile() {
        return true;
    }


    public File getNowUseFile() {
        return nowUseFile;
    }

    public void setNowUseFile(File nowUseFile) {
        this.nowUseFile = nowUseFile;
    }
}
