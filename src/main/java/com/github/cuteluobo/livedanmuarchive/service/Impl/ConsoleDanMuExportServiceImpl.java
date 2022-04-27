package com.github.cuteluobo.livedanmuarchive.service.Impl;

import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.service.DanMuExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author CuteLuoBo
 * @date 2021/12/17 16:48
 */
public class ConsoleDanMuExportServiceImpl implements DanMuExportService {

    Logger logger = LoggerFactory.getLogger(ConsoleDanMuExportServiceImpl.class);



    public ConsoleDanMuExportServiceImpl() {}




    /**
     * 打印消息到控制台
     *
     * @param data 单条数据
     * @return 是否成功
     */
    public void printMessageToConsole(DanMuData data) {
        logger.info(data.toString());
    }


    /**
     * 单弹幕导出操作
     *
     * @param danMuData 弹幕信息
     * @return 是否导出成功
     */
    @Override
    public Boolean export(DanMuData danMuData) {
        printMessageToConsole(danMuData);
        return true;
    }

    /**
     * 批量导出
     *
     * @param danMuDataList 弹幕信息列表
     * @return 是否导出成功
     */
    @Override
    public Boolean batchExport(List<DanMuData> danMuDataList) {
        for (DanMuData danMuData :
                danMuDataList) {
            printMessageToConsole(danMuData);
        }
        return null;
    }
}
