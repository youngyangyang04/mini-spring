package com.kama.minispring.beans.factory;

import com.kama.minispring.beans.BeansException;

/**
 * Bean工厂接口，定义IoC容器的基本功能
 * 
 * @author kama
 * @version 1.0.0
 */
public interface BeanFactory {
    
    /**
     * 根据bean的名称获取bean实例
     *
     * @param name bean的名称
     * @return bean实例
     * @throws BeansException 如果获取bean失败
     */
    Object getBean(String name) throws BeansException;
    
    /**
     * 根据bean的名称和类型获取bean实例
     *
     * @param name bean的名称
     * @param requiredType bean的类型
     * @return bean实例
     * @throws BeansException 如果获取bean失败
     */
    <T> T getBean(String name, Class<T> requiredType) throws BeansException;
    
    /**
     * 根据bean的类型获取bean实例
     *
     * @param requiredType bean的类型
     * @return bean实例
     * @throws BeansException 如果获取bean失败
     */
    <T> T getBean(Class<T> requiredType) throws BeansException;
    
    /**
     * 判断是否包含指定名称的bean
     *
     * @param name bean的名称
     * @return 如果包含返回true，否则返回false
     */
    boolean containsBean(String name);
    
    /**
     * 判断指定名称的bean是否为单例
     *
     * @param name bean的名称
     * @return 如果是单例返回true，否则返回false
     * @throws BeansException 如果获取bean失败
     */
    boolean isSingleton(String name) throws BeansException;
    
    /**
     * 判断指定名称的bean是否为原型
     *
     * @param name bean的名称
     * @return 如果是原型返回true，否则返回false
     * @throws BeansException 如果获取bean失败
     */
    boolean isPrototype(String name) throws BeansException;
} 