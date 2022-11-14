package com.github.cuteluobo.livedanmuarchive.command.base;

import org.jetbrains.annotations.NotNull;

import java.io.Console;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 指令中心
 *
 * @author CuteLuoBo
 * @date 2022/10/31 10:31
 */
public class CommandCenter {
    /**自动初始监听的单例*/
    public static final CommandCenter INSTANCE = new CommandCenter();
    private Console console;
    private Scanner scanner;
    private String helpTip = null;
    /**
     * 指令映射表
     */
    private Map<String,ICommand> commandMap = new HashMap<>();

    private CommandCenter() {
//        console = System.console();
//        while (console.reader()) {
//        }
//        in = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * 额外的启动监听方法，避免主方法堵塞
     */
    public void startCommandListen() {
        //监听控制台输入
        scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            //基础帮助提示
            if ("?".equalsIgnoreCase(input) || "help".equalsIgnoreCase(input)) {
                System.out.println(getHelpTip());
            } else if (execCommand(input) == null) {
                System.out.println("没有匹配的指令，请检查输入或使用help获取指令帮助");
            }
        }
    }

    /**
     * 注册指令
     * @param command 指令实现类
     * @param override 是否覆盖
     */
    public void registerCommand(@NotNull ICommand command, boolean override) {
        String[] secondCommandNameArray = command.getCommandSecondName();
        if (override) {
            commandMap.put(command.getCommandName(), command);
            if (secondCommandNameArray != null) {
                for (String s :
                        secondCommandNameArray) {
                    commandMap.put(s, command);
                }
            }
        } else {
            commandMap.putIfAbsent(command.getCommandName(), command);
            if (secondCommandNameArray != null) {
                for (String s :
                        secondCommandNameArray) {
                    commandMap.putIfAbsent(s, command);
                }
            }
        }
        //新增指令时，清空原有帮助信息以重新生成
        helpTip = null;
    }

    /**
     * 执行指令
     *
     * @param input 控制台输入
     * @return 是否执行成功
     */
    private Boolean execCommand(String input) {
        if (input != null && input.trim().length() > 0) {
            //空格分割指令
            String[] inputArray = input.split(" ");
            String mainCommandName = inputArray[0];
            ICommand command = commandMap.get(mainCommandName);
            if (command != null) {
                //简单指令
                if (command instanceof ISimpleCommand) {
                    ISimpleCommand simpleCommand = (ISimpleCommand) command;
                    //跳过第1个参数后传入
                    simpleCommand.execCommand(Arrays.stream(inputArray).skip(1).toArray(String[]::new));
                }
                //复合指令
                else if (command instanceof ICompositeCommand && inputArray.length > 1) {
                    ICompositeCommand compositeCommand = (ICompositeCommand) command;
                    //子指令
                    String subCommandName = inputArray[1];
                    //跳过前2个参数后传入
                    return compositeCommand.execSubCommand(subCommandName, Arrays.stream(inputArray).skip(2).toArray(String[]::new));
                }
            }
        }
        return null;
    }

    /**
     * 获取帮助提示
     * @return 帮助提示
     */
    private String getHelpTip() {
        //为空时进行初始化帮助提示
        if (helpTip == null) {
            StringBuilder sb = new StringBuilder();
            //默认指令
            sb.append("/?").append("\t").append("帮助").append("\n");
            sb.append("/help").append("\t").append("帮助").append("\n");
            for (Map.Entry<String, ICommand> entry : commandMap.entrySet()
            ) {
                String commandName = entry.getKey();
                ICommand command = entry.getValue();
                if (command != null) {
                    sb.append("/").append(commandName).append("\t").append(entry.getValue().getCommandDescription()).append("\n");
                }
            }
            helpTip = sb.toString();
        }
        return helpTip;
    }
}
