package com.github.cuteluobo.livedanmuarchive.command.base;

/**
 * 抽象指令父类，所有指令子类从这里派生
 *
 * @author CuteLuoBo
 * @date 2022/10/31 12:18
 */
public abstract class AbstractCommand implements ICommand{

    public AbstractCommand(String commandName, String commandDescription) {
        this.commandName = commandName;
        this.commandDescription = commandDescription;
    }

    public AbstractCommand(String commandName, String[] secondCommandName, String commandDescription) {
        this.commandName = commandName;
        this.CommandSecondName = secondCommandName;
        this.commandDescription = commandDescription;
    }

    /**
     * 指令名称
     */
    private String commandName;
    /**
     * 指令副名称
     */
    private String[] CommandSecondName;
    /**
     * 指令描述
     */
    private String commandDescription;

    @Override
    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public String[] getCommandSecondName() {
        return CommandSecondName;
    }

    public void setCommandSecondName(String[] commandSecondName) {
        CommandSecondName = commandSecondName;
    }

    @Override
    public String getCommandDescription() {
        return commandDescription;
    }

    public void setCommandDescription(String commandDescription) {
        this.commandDescription = commandDescription;
    }
}
