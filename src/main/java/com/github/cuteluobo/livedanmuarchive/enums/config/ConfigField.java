package com.github.cuteluobo.livedanmuarchive.enums.config;

import java.util.Enumeration;

/**
 * 配置字段枚举要求接口
 *
 * @author CuteLuoBo
 * @date 2022/7/5 19:05
 */
public interface ConfigField {

    /**
     * 获取字段字符串
     * @return 字段字符串
     */
    String getFieldString();

    /**
     * 获取注释
     * @return 注释
     */
    String getComment();

    /**
     * 获取字段默认值
     * @return 字段默认值
     */
    String getNormalValue();

    /**
     * 获取是否为主字段
     * @return 是否为主字段
     */
    boolean isMainField();

    /**
     * 获取列表头
     *
     * @return 列表头，为null时视为不需要生成列表
     */
    ConfigField getListHeader();


    /**
     * 获取主开头
     * @return 主开头
     */
    ConfigField getMainField();

}
