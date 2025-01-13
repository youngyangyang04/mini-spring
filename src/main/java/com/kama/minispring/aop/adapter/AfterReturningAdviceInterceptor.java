package com.kama.minispring.aop.adapter;

import com.kama.minispring.aop.AfterReturningAdvice;
import com.kama.minispring.aop.MethodInterceptor;
import com.kama.minispring.aop.MethodInvocation;

/**
 * 方法返回后通知拦截器
 * 将AfterReturningAdvice转换为MethodInterceptor
 *
 * @author kama
 * @version 1.0.0
 */
public class AfterReturningAdviceInterceptor implements MethodInterceptor {
    
    private final AfterReturningAdvice advice;
    
    /**
     * 构造函数
     *
     * @param advice 方法返回后通知
     */
    public AfterReturningAdviceInterceptor(AfterReturningAdvice advice) {
        this.advice = advice;
    }
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // 执行目标方法
        Object returnValue = invocation.proceed();
        // 执行返回后通知
        advice.afterReturning(returnValue, invocation.getMethod(), invocation.getArguments(), invocation.getThis());
        return returnValue;
    }
} 