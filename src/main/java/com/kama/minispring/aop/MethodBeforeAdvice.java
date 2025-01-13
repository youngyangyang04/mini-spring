package com.kama.minispring.aop;

import java.lang.reflect.Method;

/**
 * 方法前置通知接口
 * 在目标方法执行前执行的通知
 *
 * @author kama
 * @version 1.0.0
 */
public interface MethodBeforeAdvice extends BeforeAdvice {
    
    /**
     * 在目标方法执行前执行的操作
     *
     * @param method 目标方法
     * @param args 方法参数
     * @param target 目标对象
     * @throws Throwable 执行异常
     */
    void before(Method method, Object[] args, Object target) throws Throwable;
} 