package com.kama.minispring.aop;

/**
 * AOP代理接口
 * 定义创建代理对象的方法
 * 
 * @author kama
 * @version 1.0.0
 */
public interface AopProxy {
    
    /**
     * 获取代理对象
     * 
     * @return 代理对象
     */
    Object getProxy();
    
    /**
     * 获取代理对象
     * 
     * @param classLoader 类加载器
     * @return 代理对象
     */
    Object getProxy(ClassLoader classLoader);
} 