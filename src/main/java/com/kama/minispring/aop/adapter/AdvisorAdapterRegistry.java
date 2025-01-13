package com.kama.minispring.aop.adapter;

import com.kama.minispring.aop.Advice;
import com.kama.minispring.aop.MethodInterceptor;

/**
 * 通知适配器注册表接口
 * 管理所有的通知适配器
 *
 * @author kama
 * @version 1.0.0
 */
public interface AdvisorAdapterRegistry {
    
    /**
     * 注册通知适配器
     *
     * @param adapter 通知适配器
     */
    void registerAdvisorAdapter(AdvisorAdapter adapter);
    
    /**
     * 将通知转换为方法拦截器
     *
     * @param advice 通知
     * @return 方法拦截器
     */
    MethodInterceptor[] getInterceptors(Advice advice);
    
    /**
     * 包装通知为方法拦截器
     *
     * @param advice 通知
     * @return 方法拦截器
     */
    MethodInterceptor wrap(Advice advice);
} 