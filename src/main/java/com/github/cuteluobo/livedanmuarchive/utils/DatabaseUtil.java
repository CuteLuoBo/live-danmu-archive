package com.github.cuteluobo.livedanmuarchive.utils;

import com.github.cuteluobo.livedanmuarchive.mapper.BaseTableMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuDataModelMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuDatabaseTableMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuFormatModelMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuUserInfoModelMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.main.DanMuTaskPlanMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.main.DanmuAccountTaskMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.main.DanmuSenderTaskMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.main.MainTableMapper;
import com.github.cuteluobo.livedanmuarchive.model.*;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/**
 * 数据库常用工具方法类
 * @author CuteLuoBo
 * @date 2022/4/5 17:21
 */
public class DatabaseUtil {
    private static Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);

    /**
     * 弹幕数据库模型列表
     */
    public static final List<Class<? extends Object>> DANMU_DATABASE_MODEL_LIST = List.of(DanMuUserInfoModel.class,DanMuDataModel.class,DanMuFormatModel.class);
    /**
     * 弹幕数据库Mapper列表
     */
    public static final List<Class<? extends Object>> DANMU_DATABASE_MAPPER_LIST =
            List.of(DanMuDatabaseTableMapper.class,DanMuDataModelMapper.class,DanMuFormatModelMapper.class,DanMuUserInfoModelMapper.class);

    /**
     * 主数据库模型列表
     */
    public static final List<Class<? extends Object>> MAIN_DATABASE_MODEL_LIST = List.of(DanmuAccountTaskModel.class, DanmuSenderTaskModel.class);
    /**
     * 主数据库Mapper列表
     * TODO 新增Mapper时都应在此列表中添加
     */
    public static final List<Class<? extends Object>> MAIN_DATABASE_MAPPER_LIST =
            List.of(MainTableMapper.class,DanmuAccountTaskMapper.class, DanmuSenderTaskMapper.class, DanMuTaskPlanMapper.class);
    /**
     * 获取默认的SQLITE数据库数据源配置
     * @param file 需要读取的sqlite数据库文件
     * @return 数据源对象
     */
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

    /**
     * 初始化与配置数据库连接工厂
     * 参考 https://mybatis.org/mybatis-3/zh/java-api.html#sqlSessions
     * @param databaseFile 当前使用的数据库文件
     * @return 创建完成的SQL对话工厂对象
     */
    public static SqlSessionFactory initFileDatabaseConnectFactory(@NotNull File databaseFile,@NotNull  List<Class<? extends Object>> modelList,@NotNull  List<Class<? extends Object>> mapperList) {
        //配置数据源
        DataSource dataSource = getNormalSqliteDatasource(databaseFile);
        //JDBC事务管理器
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        //mybatis环境变量，配置ID,事务管理器，数据源
        Environment environment = new Environment(databaseFile.getName(), transactionFactory, dataSource);
        //设置mybatis链接配置
        Configuration configuration = new Configuration(environment);
        //启用懒加载
        configuration.setLazyLoadingEnabled(true);
        //注册表模型
        modelList.forEach(m -> configuration.getTypeAliasRegistry().registerAlias(m));
        //注册操作mapper，xml文件应跟mapper放在同一个包中，https://stackoverflow.com/questions/58522647/add-xml-mapper-to-the-configuration-of-mybatis-in-the-java-code-with-path-differ
        //打包时此包名注册无效
//        configuration.addMappers("com.github.cuteluobo.livedanmuarchive.mapper");
        //手动指定mapper类型
        mapperList.forEach(configuration::addMapper);
        //开启驼峰
        configuration.setMapUnderscoreToCamelCase(true);
        //构建session工厂
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        return sqlSessionFactoryBuilder.build(configuration);
    }

    /**
     * 检查和创建表
     */
    public static boolean checkAndCreateTable(@NotNull SqlSessionFactory sqlSessionFactory, boolean isNewDatabaseFile, Class<? extends BaseTableMapper> tableMapperClass) {
        //创建sql对话
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//            Statement stmt = sqlSession.getConnection().createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT sqlite_version()");
//            if (rs.next()) {
//                // 应显示驱动内置版本（如3.50.2）
//                System.out.println("实际引擎版本: " + rs.getString(1));
//            }
            BaseTableMapper baseTableMapper = sqlSession.getMapper(tableMapperClass);
            //新数据库时直接创建，否则验证
            if (isNewDatabaseFile) {
                logger.debug("判断为新创建数据库，略过检验直接建表");
            } else {
                logger.debug("判断为旧数据库，校验表并补充");
            }
            try {
                baseTableMapper.createAllTable(isNewDatabaseFile);
            } catch (Exception exception) {
                logger.error("数据库表检验与新建失败",exception);
                throw exception;
            }
            sqlSession.commit();
            return true;
            }
    }
}
