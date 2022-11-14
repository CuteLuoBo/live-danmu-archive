package com.github.cuteluobo.livedanmuarchive.service;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 弹幕格式化导出（针对对外导出而不是内部储存）
 *
 * @author CuteLuoBo
 * @param <T> 导出的包装对象
 */
public interface DanMuFormatExportService<T> {
    /**
     * 导出所有弹幕结果
     * @throws IOException IO错误
     * @return 导出的指定包装对象
     */
    default T formatExportAll() throws IOException {
        return formatExportBySelector(null, null);
    }

    /**
     * 导出指定筛选的弹幕结果
     * @param startTime 开始时间,为null时为不限
     * @param endTime   结束时间,为null时为不限
     * @throws IOException IO错误
     * @return 导出的指定包装对象
     */
    T formatExportBySelector(LocalDateTime startTime, LocalDateTime endTime) throws IOException;
}
