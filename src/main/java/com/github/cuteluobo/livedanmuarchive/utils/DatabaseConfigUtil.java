package com.github.cuteluobo.livedanmuarchive.utils;

import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.util.Properties;

/**
 * 数据库配置工具栏
 * @author CuteLuoBo
 * @date 2022/4/5 17:21
 */
public class DatabaseConfigUtil {
    public static DataSource getNormalSqliteDatasource(File file) {
        Properties properties= new Properties();
        properties.put("driver", "org.sqlite.JDBC");
        properties.put("username", "");
        properties.put("password", "");
        properties.put("url", "jdbc:sqlite:"+file.getAbsolutePath());
        DataSourceFactory dataSourceFactory = new PooledDataSourceFactory();
        dataSourceFactory.setProperties(properties);
        return dataSourceFactory.getDataSource();
    }
}
