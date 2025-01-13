package com.kama.minispring.aop.adapter;

import com.kama.minispring.aop.Advice;
import com.kama.minispring.aop.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认的通知适配器注册表实现
 * 管理所有的通知适配器，并提供通知转换功能
 *
 * @author kama
 * @version 1.0.0
 */
public class DefaultAdvisorAdapterRegistry implements AdvisorAdapterRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultAdvisorAdapterRegistry.class);
    
    private final List<AdvisorAdapter> adapters = new ArrayList<>();
    
    /**
     * 构造函数
     * 注册默认的通知适配器
     */
    public DefaultAdvisorAdapterRegistry() {
        registerAdvisorAdapter(new MethodBeforeAdviceAdapter());
        registerAdvisorAdapter(new AfterReturningAdviceAdapter());
    }
    
    @Override
    public void registerAdvisorAdapter(AdvisorAdapter adapter) {
        adapters.add(adapter);
    }
    
    @Override
    public MethodInterceptor[] getInterceptors(Advice advice) {
        List<MethodInterceptor> interceptors = new ArrayList<>();
        
        // 如果已经是MethodInterceptor，直接添加
        if (advice instanceof MethodInterceptor) {
            interceptors.add((MethodInterceptor) advice);
        }
        
        // 遍历所有适配器，找到支持该通知的适配器
        for (AdvisorAdapter adapter : adapters) {
            if (adapter.supportsAdvice(advice)) {
                interceptors.add(adapter.getInterceptor(advice));
            }
        }
        
        return interceptors.toArray(new MethodInterceptor[0]);
    }
    
    @Override
    public MethodInterceptor wrap(Advice advice) {
        if (advice instanceof MethodInterceptor) {
            return (MethodInterceptor) advice;
        }
        
        // 遍历所有适配器，找到支持该通知的适配器
        for (AdvisorAdapter adapter : adapters) {
            if (adapter.supportsAdvice(advice)) {
                return adapter.getInterceptor(advice);
            }
        }
        
        throw new IllegalArgumentException("Advice type [" + advice.getClass().getName() + 
                "] is not supported by any registered adapter");
    }
} 