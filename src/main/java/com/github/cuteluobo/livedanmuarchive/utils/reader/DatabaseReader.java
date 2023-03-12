package com.github.cuteluobo.livedanmuarchive.utils.reader;

import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

/**
 * 数据库读取类
 * @author CuteLuoBo
 * @date 2023/3/9 12:18
 */
public abstract class DatabaseReader<T, K> implements IDatabaseReader<T,K>{
    private SqlSessionFactory sqlSessionFactory;
    private long index;
    private long total;
    private long end;
    public DatabaseReader(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }
}
