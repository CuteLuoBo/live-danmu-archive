package com.github.cuteluobo.livedanmuarchive.command.base;

/**
 * 抽象简单指令父类
 * @author CuteLuoBo
 * @date 2022/10/31 12:25
 */
public abstract class AbstractSimpleCommand extends AbstractCommand implements ISimpleCommand {

    public AbstractSimpleCommand(String commandName, String commandDescription) {
        super(commandName, commandDescription);
    }

    public AbstractSimpleCommand(String commandName, String[] secondCommandName, String commandDescription) {
        super(commandName, secondCommandName, commandDescription);
    }
}
