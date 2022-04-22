package com.github.cuteluobo.livedanmuarchive.service;

import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;

import java.io.IOException;
import java.util.List;

/**
 * 弹幕导出接口
 *
 * @author CuteLuoBo
 */
public interface DanMuExportService {

    /**
     * 单弹幕导出操作
     * @param danMuData 弹幕信息
     * @return 是否导出成功
     * @throws IOException 导出时发生的IO错误
     */
    Boolean export(DanMuData danMuData) throws IOException;

    /**
     * 批量导出
     * @param danMuDataList 弹幕信息列表
     * @return 是否导出成功
     */
    Boolean batchExport(List<DanMuData> danMuDataList);
}
