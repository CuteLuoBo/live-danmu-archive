package com.github.cuteluobo.livedanmuarchive.command.base;

/**
 * 基础指令信息接口，不能直接使用
 * @author CuteLuoBo
 * @date 2022/10/31 10:36
 */
public interface ICommand {
    /**
     * 获取指令名称
     * @return 指令名称
     */
    String getCommandName();

    /**
     * 获取次要指令名称
     * @return 次要指令名称
     */
    String[] getCommandSecondName();

    /**
     * 获取指令描述
     * @return 指令描述
     */
    String getCommandDescription();
}
