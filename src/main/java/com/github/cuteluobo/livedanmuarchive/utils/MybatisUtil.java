package com.github.cuteluobo.livedanmuarchive.utils;

import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuDataModelMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuDatabaseTableMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuFormatModelMapper;
import com.github.cuteluobo.livedanmuarchive.mapper.danmu.DanMuUserInfoModelMapper;
import com.github.cuteluobo.livedanmuarchive.model.DanMuDataModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuFormatModel;
import com.github.cuteluobo.livedanmuarchive.model.DanMuUserInfoModel;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.io.File;

/**
 * mybatis相关工具类
 * @author CuteLuoBo
 * @date 2022/11/13 11:25
 */
public class MybatisUtil {

    /**
     * 初始化与配置数据库连接工厂
     * 参考 https://mybatis.org/mybatis-3/zh/java-api.html#sqlSessions
     * @param databaseFile 当前使用的数据库文件
     * @return 创建完成的SQL对话工厂对象
     */
    public static SqlSessionFactory initFileDatabaseConnectFactory(File databaseFile) {
        //配置数据源
        DataSource dataSource = DatabaseConfigUtil.getNormalSqliteDatasource(databaseFile);
        //JDBC事务管理器
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        //mybatis环境变量，配置ID,事务管理器，数据源
        Environment environment = new Environment(databaseFile.getName(), transactionFactory, dataSource);
        //设置mybatis链接配置
        Configuration configuration = new Configuration(environment);
        //启用懒加载
        configuration.setLazyLoadingEnabled(true);
        //注册表模型
        configuration.getTypeAliasRegistry().registerAlias(DanMuUserInfoModel.class);
        configuration.getTypeAliasRegistry().registerAlias(DanMuDataModel.class);
        configuration.getTypeAliasRegistry().registerAlias(DanMuFormatModel.class);
        //注册操作mapper，xml文件应跟mapper放在同一个包中，https://stackoverflow.com/questions/58522647/add-xml-mapper-to-the-configuration-of-mybatis-in-the-java-code-with-path-differ
        //打包时此包名注册无效
//        configuration.addMappers("com.github.cuteluobo.livedanmuarchive.mapper.danmu");
        //手动指定mapper类型
        configuration.addMapper(DanMuDatabaseTableMapper.class);
        configuration.addMapper(DanMuDataModelMapper.class);
        configuration.addMapper(DanMuFormatModelMapper.class);
        configuration.addMapper(DanMuUserInfoModelMapper.class);
        //构建session工厂
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        return sqlSessionFactoryBuilder.build(configuration);
    }
}
