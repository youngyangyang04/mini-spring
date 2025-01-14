package com.kama.minispring.cloud.circuit;

import java.util.function.Supplier;

/**
 * 熔断器接口
 * 
 * @author kama
 * @version 1.0.0
 */
public interface CircuitBreaker<T> {
    
    /**
     * 熔断器状态枚举
     */
    enum State {
        /**
         * 关闭状态 - 允许请求通过
         */
        CLOSED,
        
        /**
         * 开启状态 - 快速失败
         */
        OPEN,
        
        /**
         * 半开状态 - 允许有限的请求通过以探测服务是否恢复
         */
        HALF_OPEN
    }
    
    /**
     * 执行受保护的操作
     *
     * @param operation 要执行的操作
     * @param fallback 降级操作
     * @return 操作结果
     */
    T execute(Supplier<T> operation, Supplier<T> fallback);
    
    /**
     * 获取当前状态
     *
     * @return 熔断器状态
     */
    State getState();
    
    /**
     * 重置熔断器状态
     */
    void reset();
    
    /**
     * 获取熔断器名称
     *
     * @return 熔断器名称
     */
    String getName();
} 