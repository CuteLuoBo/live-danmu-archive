package com.github.cuteluobo.livedanmuarchive.service;

import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;

import java.util.List;

/**
 * 拓展弹幕导出接口
 * @author CuteLuoBo
 * @date 2022/2/11 16:08
 */
public interface ExDanMuExportService extends DanMuExportService {
    /**
     * 批量导出
     * @param danMuDataList 弹幕信息列表
     * @return 是否导出成功
     */
    Boolean batchExport(List<DanMuData> danMuDataList);
}
