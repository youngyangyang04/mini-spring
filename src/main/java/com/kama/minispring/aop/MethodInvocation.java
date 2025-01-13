package com.kama.minispring.aop;

import java.lang.reflect.Method;

/**
 * 方法调用接口
 * 封装方法调用的上下文信息
 *
 * @author kama
 * @version 1.0.0
 */
public interface MethodInvocation {

    /**
     * 获取目标方法
     *
     * @return 目标方法
     */
    Method getMethod();

    /**
     * 获取方法参数
     *
     * @return 方法参数数组
     */
    Object[] getArguments();

    /**
     * 获取目标对象
     *
     * @return 目标对象
     */
    Object getThis();

    /**
     * 执行方法调用
     *
     * @return 方法返回值
     * @throws Throwable 执行异常
     */
    Object proceed() throws Throwable;
} 