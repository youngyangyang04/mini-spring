package com.kama.minispring.aop.adapter;

import com.kama.minispring.aop.Advice;
import com.kama.minispring.aop.MethodBeforeAdvice;
import com.kama.minispring.aop.MethodInterceptor;

/**
 * 方法前置通知适配器
 * 用于将MethodBeforeAdvice转换为MethodInterceptor
 *
 * @author kama
 * @version 1.0.0
 */
public class MethodBeforeAdviceAdapter implements AdvisorAdapter {
    
    @Override
    public boolean supportsAdvice(Advice advice) {
        return advice instanceof MethodBeforeAdvice;
    }
    
    @Override
    public MethodInterceptor getInterceptor(Advice advice) {
        return new MethodBeforeAdviceInterceptor((MethodBeforeAdvice) advice);
    }
} 