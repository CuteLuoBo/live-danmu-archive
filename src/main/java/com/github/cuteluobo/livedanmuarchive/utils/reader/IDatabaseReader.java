package com.github.cuteluobo.livedanmuarchive.utils.reader;

import java.util.List;

/**
 * 数据库读取类-接口
 *
 * @author CuteLuoBo
 * @date 2023/3/9 12:14
 */
public interface IDatabaseReader<T,K> {
    /**
     * 读取下一个数据
     * @return 读取到的数据，无下一个数据时，返回null
     */
    T readNext();

    /**
     * 读取符合筛选条件的数据列表
     * @param select 筛选条件
     * @return 查询到的数据列表
     */
    List<T> readList(K select);
}
