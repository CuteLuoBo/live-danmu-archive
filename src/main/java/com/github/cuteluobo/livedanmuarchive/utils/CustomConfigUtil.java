package com.github.cuteluobo.livedanmuarchive.utils;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlSequenceBuilder;
import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendAccountField;
import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigDanMuAutoSendTaskField;
import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigField;
import com.github.cuteluobo.livedanmuarchive.enums.config.ConfigRecordField;
import com.github.cuteluobo.livedanmuarchive.enums.ExportPattern;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuExportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 配置文件工具栏
 * @author CuteLuoBo
 * @date 2022/3/29 13:45
 */
public class CustomConfigUtil {
    Logger logger = LoggerFactory.getLogger(CustomConfigUtil.class);

    /**
     * 默认文件路径
     */
    private static final File NORMAL_FILE = new File("config.yaml");

    /**
     * 单例模式
     */
    public static final CustomConfigUtil INSTANCE = new CustomConfigUtil();

    private File usageFile = NORMAL_FILE;

    /**
     * 缓存的配置文件
     */
    private YamlMapping configMapping;

    private CustomConfigUtil(){
        loadConfigMapping(NORMAL_FILE);
    }

    /**
     * 加载配置文件，后续看情况是否要公开自定义配置路径接口
     * @param file
     */
    private void loadConfigMapping(File file) {
        if (file.exists()) {
            try {
                logger.debug("读取配置文件,路径：{}", file.getAbsolutePath());
                configMapping = Yaml.createYamlInput(file).readYamlMapping();
                usageFile = file;
            } catch (IOException e) {
                logger.error("读取配置文件时出现错误", e);
                createYamlFile(file);
            }
        } else {
            createYamlFile(file);
        }
    }

    /**
     * 对外暴露的重载配置文件方法
     */
    public void reloadConfig() {
        String pathString = usageFile.getAbsolutePath();
        if (usageFile.exists()) {
            try {
                logger.info("尝试重新读取配置文件,路径：{}", pathString);
                configMapping = Yaml.createYamlInput(usageFile).readYamlMapping();
            } catch (IOException e) {
                logger.error("读取配置文件时出现错误", e);
            }
        } else {
            logger.error("需重载的配置文件不存在，路径：{}", pathString);
        }
    }

    /**
     * 创建配置文件
     * @param file 文件路径
     */
    private void createYamlFile(File file){
        logger.info("尝试创建配置文件...");
        YamlMapping yaml = getInitConfigMapping();
        //检查新建文件
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ioException) {
                logger.error("新建配置文件失败，请检查路径:{}，抛出错误:{}",file.getAbsolutePath(),ioException);
                System.exit(0);
            }
        }
        //检查写入
        if (!file.canWrite()) {
            logger.error("配置文件无法写入，请检查文件是否被占用：{}",file.getAbsolutePath());
            System.exit(0);
        }
        //尝试写入
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(yaml.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
            logger.info("配置文件创建完成，请填写配置项后重新启动程序！，路径:{}",file.getAbsolutePath());
            System.exit(0);
        }catch (IOException ioException) {
            logger.error("配置文件创建失败，路径：{}，抛出错误:{}",file.getAbsolutePath(),ioException);
            System.exit(0);
        }

    }
    /**
     * 获取初始化的配置Mapping数据
     * @return 初始化的配置Mapping数据
     */
    public YamlMapping getInitConfigMapping() {
        YamlMappingBuilder configBuilder = Yaml.createYamlMappingBuilder()
                .add("version", Yaml.createYamlScalarBuilder()
                        .addLine("1.2.0")
                        .buildPlainScalar("配置文件版本号"))
                //数据源配置部分
                .add(ConfigRecordField.MAIN_FIELD.getFieldString(), Yaml.createYamlMappingBuilder()
                        .add(ConfigRecordField.RECORD_LIST.getFieldString(), Yaml.createYamlSequenceBuilder()
                                //默认添加两个作为实例
                                .add(
                                        createNormalRecordListUnit()
                                )
                                .add(
                                        createNormalRecordListUnit()
                                )
                                .build(ConfigRecordField.RECORD_LIST.getComment()))
                        .build(ConfigRecordField.MAIN_FIELD.getComment()));
        configBuilder = appendConfigChunk(configBuilder, ConfigDanMuAutoSendTaskField.values(), 1);
        configBuilder = appendConfigChunk(configBuilder, ConfigDanMuAutoSendAccountField.values(), 2);
        return configBuilder.build();
    }

    /**
     * 构建默认录制列表单个分块
     * @return
     */
    public static YamlMapping createNormalRecordListUnit() {
        return Yaml.createYamlMappingBuilder()
                .add(ConfigRecordField.ROOM_URL.getFieldString(), Yaml.createYamlScalarBuilder()
                        .addLine("http(s)://xxx.com/xxx")
                        .buildPlainScalar(ConfigRecordField.ROOM_URL.getComment()))
                .add(ConfigRecordField.RECORD_TYPE.getFieldString(), Yaml.createYamlScalarBuilder()
                        .addLine("danmu")
                        .buildPlainScalar(ConfigRecordField.RECORD_TYPE.getComment()))
                .add(ConfigRecordField.SAVE_NAME.getFieldString(), Yaml.createYamlScalarBuilder()
                        .addLine("xx保存名称")
                        .buildPlainScalar(ConfigRecordField.SAVE_NAME.getComment()))
                .add(ConfigRecordField.EXPORT_PATTERN.getFieldString(), Yaml.createYamlScalarBuilder()
                        .addLine(ExportPattern.ALL_COLLECT.getText())
                        .buildPlainScalar(ConfigRecordField.EXPORT_PATTERN.getComment()))
                .add(ConfigRecordField.DANMU_EXPORT_TYPE.getFieldString(), Yaml.createYamlScalarBuilder()
                        .addLine(DanMuExportType.SQLITE.getValue())
                        .buildPlainScalar(ConfigRecordField.DANMU_EXPORT_TYPE.getComment()))
                .add(ConfigRecordField.DANMU_RECORD_RETRY_TIME.getFieldString(), Yaml.createYamlScalarBuilder()
                        .addLine("300")
                        .buildPlainScalar(ConfigRecordField.DANMU_RECORD_RETRY_TIME.getComment()))
                .build(ConfigRecordField.RECORD_LIST_UNIT.getComment());
    }


    /**
     * 根据枚举列表自动添加配置块
     *
     * @param yamlMappingBuilder Yaml构造器
     * @param configFieldArray    配置字段数组
     * @param normalUnitNum      示例实例数量
     */
    private YamlMappingBuilder appendConfigChunk(YamlMappingBuilder yamlMappingBuilder, ConfigField[] configFieldArray,int normalUnitNum) {
        //注：因Yaml...Builder的add方法不修改对象自身，只返回修改后创建的新对象，所以需要重赋值
        //当传入错误时，不执行
        if (yamlMappingBuilder == null || configFieldArray == null || configFieldArray.length == 0) {
            return yamlMappingBuilder;
        }
        //取出不为开头和列表表头的主列表
        List<ConfigField> mainFieldList = Arrays.stream(configFieldArray)
                .filter(cf -> cf.isMainField() && cf != cf.getMainField() && cf != cf.getListHeader())
                .collect(Collectors.toList());
        List<ConfigField> unitFieldList = Arrays.stream(configFieldArray).filter(cf ->!cf.isMainField()).collect(Collectors.toList());
        //主属性
        YamlMappingBuilder mainBuilder = Yaml.createYamlMappingBuilder();
        for (ConfigField cf :
                mainFieldList) {
            mainBuilder = mainBuilder.add(cf.getFieldString(), Yaml.createYamlScalarBuilder()
                    .addLine(cf.getNormalValue())
                    .buildPlainScalar(cf.getComment()));
        }
        //有列表头时生成列表相关数据
        ConfigField listHeader = configFieldArray[0].getListHeader();
        if (listHeader != null) {
            //先构造单体
            YamlMappingBuilder unitBuilder = Yaml.createYamlMappingBuilder();
            for (ConfigField cf : unitFieldList
            ) {
                unitBuilder = unitBuilder.add(cf.getFieldString(), Yaml.createYamlScalarBuilder()
                        .addLine(cf.getNormalValue())
                        .buildPlainScalar(cf.getComment()));
            }
            YamlMapping unitMapping = unitBuilder.build();
            YamlSequenceBuilder yamlSequenceBuilder = Yaml.createYamlSequenceBuilder();
            for (int i = 0; i < normalUnitNum; i++) {
                yamlSequenceBuilder = yamlSequenceBuilder.add(unitMapping);
            }
            mainBuilder = mainBuilder.add(listHeader.getFieldString(), yamlSequenceBuilder.build(listHeader.getComment()));
        }
        ConfigField mainField = configFieldArray[0].getMainField();
        return yamlMappingBuilder.add(mainField.getFieldString(),mainBuilder.build(mainField.getComment()));
    }

    public YamlMapping getConfigMapping() {
        return configMapping;
    }

    public void setConfigMapping(YamlMapping configMapping) {
        this.configMapping = configMapping;
    }
}