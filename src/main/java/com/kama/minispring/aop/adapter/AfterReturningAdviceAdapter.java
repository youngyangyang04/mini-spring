package com.kama.minispring.aop.adapter;

import com.kama.minispring.aop.Advice;
import com.kama.minispring.aop.AfterReturningAdvice;
import com.kama.minispring.aop.MethodInterceptor;

/**
 * 方法返回后通知适配器
 * 用于将AfterReturningAdvice转换为MethodInterceptor
 *
 * @author kama
 * @version 1.0.0
 */
public class AfterReturningAdviceAdapter implements AdvisorAdapter {
    
    @Override
    public boolean supportsAdvice(Advice advice) {
        return advice instanceof AfterReturningAdvice;
    }
    
    @Override
    public MethodInterceptor getInterceptor(Advice advice) {
        return new AfterReturningAdviceInterceptor((AfterReturningAdvice) advice);
    }
} 