package com.github.cuteluobo.livedanmuarchive.command.base;

import com.github.cuteluobo.livedanmuarchive.command.base.action.SubCommandReg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽象复合指令
 * @author CuteLuoBo
 * @date 2022/10/31 12:29
 */
public abstract class AbstractCompositeCommand extends AbstractCommand implements ICompositeCommand {
    Logger logger = LoggerFactory.getLogger(AbstractCompositeCommand.class);
    /**
     * 子指令映射表
     */
    private Map<String, Method> subCommandMap;

    public AbstractCompositeCommand(String commandName, String commandDescription) {
        super(commandName, commandDescription);
        regAllSubCommand();
    }

    public AbstractCompositeCommand(String commandName, String[] secondCommandName, String commandDescription) {
        super(commandName, secondCommandName, commandDescription);
        regAllSubCommand();
    }

    /**
     * 内部通过反射注册子指令映射
     */
    private void regAllSubCommand() {
        Class<? extends AbstractCompositeCommand> commandClass = this.getClass();
        Method[] methods = commandClass.getMethods();
        subCommandMap = new HashMap<>(methods.length);
        for (Method m :
                methods) {
            //优先使用子指令注解标识的子指令字符串
            SubCommandReg subCommandReg = m.getAnnotation(SubCommandReg.class);
            if (subCommandReg != null) {
                String[] subCommandNameArray = subCommandReg.subCommandName();
                if (subCommandNameArray != null && subCommandNameArray.length > 0) {
                    for (String n : subCommandNameArray
                    ) {
                        subCommandMap.put(n, m);
                    }
                    continue;
                }
            }
            //其他情况时，使用执行方法名称 (错误方法,会导致Object原生方法也会写入)
//            subCommandMap.put(m.getName(), m);
        }
    }

    /**
     * 执行子指令
     *
     * @param subCommandName 子指令名称
     * @param args           指令参数
     * @return 指令执行结果
     */
    @Override
    public boolean execSubCommand(String subCommandName, String... args) {
        //TODO 增加输入指令为空时，直接输出提示或跳转到指令实现类的help帮助功能
        Method method = subCommandMap.get(subCommandName);
        if (method != null) {
            try {
                Object result = method.invoke(this, (Object) args);
                //有非false返回结果时，返回true
                if (result instanceof Boolean) {
                    return (Boolean) result;
                }
                return true;
            }catch (InvocationTargetException | IllegalAccessException e) {
                logger.error("通过反射调调用方法错误",e);
            }
        }
        return false;
    }
}
