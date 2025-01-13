package com.kama.minispring.beans.factory.config;

/**
 * 单例bean注册表接口
 * 定义了管理单例bean的基本操作
 *
 * @author kama
 * @version 1.0.0
 */
public interface SingletonBeanRegistry {

    /**
     * 注册一个单例bean
     *
     * @param beanName bean名称
     * @param singletonObject bean实例
     */
    void registerSingleton(String beanName, Object singletonObject);

    /**
     * 获取单例bean
     *
     * @param beanName bean名称
     * @return bean实例，如果不存在返回null
     */
    Object getSingleton(String beanName);

    /**
     * 判断是否包含指定名称的单例bean
     *
     * @param beanName bean名称
     * @return 如果包含返回true，否则返回false
     */
    boolean containsSingleton(String beanName);

    /**
     * 获取所有单例bean的名称
     *
     * @return 单例bean名称数组
     */
    String[] getSingletonNames();

    /**
     * 获取单例bean的数量
     *
     * @return 单例bean的数量
     */
    int getSingletonCount();
} 