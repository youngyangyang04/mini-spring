package com.kama.minispring.boot.autoconfigure;

/**
 * 自动配置接口，所有自动配置类都需要实现此接口
 * 
 * @author kama
 * @version 1.0.0
 */
public interface AutoConfiguration {
    
    /**
     * 配置方法，实现具体的自动配置逻辑
     */
    void configure();
} 