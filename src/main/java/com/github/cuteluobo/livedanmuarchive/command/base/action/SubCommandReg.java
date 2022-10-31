package com.github.cuteluobo.livedanmuarchive.command.base.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 子指令注册注解
 *
 * @author CuteLuoBo
 * @time 2022-10-31 12:30
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommandReg {
    /**
     * @return 子指令名称列表
     */
    String[] subCommandName() default {};
}
