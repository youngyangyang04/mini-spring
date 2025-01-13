package com.kama.minispring.beans.factory.config;

import com.kama.minispring.beans.factory.ListableBeanFactory;

/**
 * 配置接口，由大多数可列举的bean工厂实现
 * 除了bean工厂的客户端方法，还提供了配置bean工厂的工具
 *
 * @author kama
 * @version 1.0.0
 */
public interface ConfigurableListableBeanFactory extends ListableBeanFactory, AutoCloseable {
    
    /**
     * 设置bean类加载器
     *
     * @param beanClassLoader 类加载器
     */
    void setBeanClassLoader(ClassLoader beanClassLoader);
    
    /**
     * 获取bean类加载器
     *
     * @return 类加载器
     */
    ClassLoader getBeanClassLoader();
    
    /**
     * 预实例化所有非延迟加载的单例
     */
    void preInstantiateSingletons();
    
    /**
     * 销毁单例bean
     */
    void destroySingletons();
    
    /**
     * 注册一个可销毁的bean
     *
     * @param beanName bean名称
     * @param bean bean实例
     */
    void registerDisposableBean(String beanName, Object bean);
    
    @Override
    void close() throws Exception;
} 