package com.kama.minispring.aop.adapter;

import com.kama.minispring.aop.Advice;
import com.kama.minispring.aop.MethodInterceptor;

/**
 * 通知适配器接口
 * 用于将不同类型的通知转换为MethodInterceptor
 *
 * @author kama
 * @version 1.0.0
 */
public interface AdvisorAdapter {
    
    /**
     * 判断是否支持给定的通知
     *
     * @param advice 通知
     * @return 是否支持
     */
    boolean supportsAdvice(Advice advice);
    
    /**
     * 将通知转换为方法拦截器
     *
     * @param advice 通知
     * @return 方法拦截器
     */
    MethodInterceptor getInterceptor(Advice advice);
} 