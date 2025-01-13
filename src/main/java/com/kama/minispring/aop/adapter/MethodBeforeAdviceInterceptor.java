package com.kama.minispring.aop.adapter;

import com.kama.minispring.aop.MethodBeforeAdvice;
import com.kama.minispring.aop.MethodInterceptor;
import com.kama.minispring.aop.MethodInvocation;

/**
 * 方法前置通知拦截器
 * 将MethodBeforeAdvice转换为MethodInterceptor
 *
 * @author kama
 * @version 1.0.0
 */
public class MethodBeforeAdviceInterceptor implements MethodInterceptor {
    
    private final MethodBeforeAdvice advice;
    
    /**
     * 构造函数
     *
     * @param advice 方法前置通知
     */
    public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
        this.advice = advice;
    }
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // 执行前置通知
        advice.before(invocation.getMethod(), invocation.getArguments(), invocation.getThis());
        // 执行目标方法
        return invocation.proceed();
    }
} 