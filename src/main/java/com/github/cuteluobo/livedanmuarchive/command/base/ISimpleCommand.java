package com.github.cuteluobo.livedanmuarchive.command.base;

/**
 * 只支持一种功能的指令参数
 * @author CuteLuoBo
 * @date 2022/10/31 10:42
 */
public interface ISimpleCommand extends ICommand {
    /**
     * 执行指令
     * @param args 指令参数
     * @return 执行结果
     */
    boolean execCommand(String... args);
}
