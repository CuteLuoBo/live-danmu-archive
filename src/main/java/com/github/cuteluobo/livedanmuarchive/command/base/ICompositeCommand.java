package com.github.cuteluobo.livedanmuarchive.command.base;

/**
 * 可执行多个子指令的合成指令接口
 * @author CuteLuoBo
 * @date 2022/10/31 10:44
 */
public interface ICompositeCommand extends ICommand{
    /**
     * 执行子指令
     * @param subCommandName 子指令名称
     * @param args           指令参数
     * @return 指令执行结果
     */
    boolean execSubCommand(String subCommandName, String... args);
}
