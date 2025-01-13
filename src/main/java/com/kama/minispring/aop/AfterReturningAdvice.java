package com.kama.minispring.aop;

import java.lang.reflect.Method;

/**
 * 方法返回后通知接口
 * 在目标方法正常返回后执行的通知
 *
 * @author kama
 * @version 1.0.0
 */
public interface AfterReturningAdvice extends AfterAdvice {
    
    /**
     * 在目标方法正常返回后执行的操作
     *
     * @param returnValue 返回值
     * @param method 目标方法
     * @param args 方法参数
     * @param target 目标对象
     * @throws Throwable 执行异常
     */
    void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
} 