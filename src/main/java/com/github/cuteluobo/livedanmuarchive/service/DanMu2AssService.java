package com.github.cuteluobo.livedanmuarchive.service;

import com.github.cuteluobo.livedanmuarchive.dto.DanMuDataModelSelector;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuFormat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 弹幕转ASS文件 接口
 * @author CuteLuoBo
 * @date 2022/10/31 15:09
 */
public interface DanMu2AssService {
    /**
     * 导出到ASS文件
     * @param videoStartTime         视频起始时间
     * @param danMuFormatList        弹幕样式列表数据,为null时全部使用默认
     * @param danMuDataList          弹幕数据列表
     * @throws IOException 导出时出现IO错误
     */
    void exportToAssFile(long videoStartTime,List<DanMuFormat> danMuFormatList, List<DanMuData> danMuDataList) throws IOException;
}
