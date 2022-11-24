package com.github.cuteluobo.livedanmuarchive;

import com.github.cuteluobo.livedanmuarchive.command.base.CommandCenter;
import com.github.cuteluobo.livedanmuarchive.command.impl.ExportCommand;
import com.github.cuteluobo.livedanmuarchive.command.impl.ExitCommand;
import com.github.cuteluobo.livedanmuarchive.controller.DanMuRecordController;

/**
 * 主程序入口
 * @author CuteLuoBo
 */
public class Application {

    public static void main(String[] args){
//        System.setProperty("javax.net.ssl.trustStoreType","Windows-ROOT");
        regCommand();
        //初始化控制器并读取任务配置文件
        DanMuRecordController danMuRecordController = DanMuRecordController.getInstance();
        danMuRecordController.addTaskByNormalConfigFile();
        //启动控制台指令监听
        CommandCenter.INSTANCE.startCommandListen();
    }

    /**
     * 注册指令
     */
    private static void regCommand() {
        CommandCenter.INSTANCE.registerCommand(new ExitCommand(), false);
        CommandCenter.INSTANCE.registerCommand(new ExportCommand(), false);
        //新指令须在此注册
    }
}
