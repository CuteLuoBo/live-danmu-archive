package com.github.cuteluobo.livedanmuarchive.command.impl;

import com.github.cuteluobo.livedanmuarchive.command.base.AbstractSimpleCommand;
import com.github.cuteluobo.livedanmuarchive.controller.DanMuRecordController;
import com.github.cuteluobo.livedanmuarchive.controller.DanMuSenderController;

/**
 * @author CuteLuoBo
 * @date 2022/10/31 13:14
 */
public class ExitCommand extends AbstractSimpleCommand  {
    public ExitCommand() {
        super("stop", "中止所有任务并退出程序");
    }

    /**
     * 执行指令
     *
     * @param args 指令参数
     * @return 执行结果
     */
    @Override
    public boolean execCommand(String... args) {
        System.out.println("正在中止弹幕录制任务...");
        DanMuRecordController.getInstance().stopAllTask();
        DanMuSenderController.getInstance().stopTask();
        //+more...
        System.out.println("所有任务中止，程序结束");
        System.exit(0);
        return true;
    }
}
