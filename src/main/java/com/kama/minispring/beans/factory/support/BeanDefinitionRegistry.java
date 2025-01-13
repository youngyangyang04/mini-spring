package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.config.BeanDefinition;

/**
 * Bean定义注册表接口
 * 定义了注册和获取bean定义的基本操作
 *
 * @author kama
 * @version 1.0.0
 */
public interface BeanDefinitionRegistry {

    /**
     * 注册一个新的bean定义
     *
     * @param beanName bean名称
     * @param beanDefinition bean定义
     * @throws BeansException 如果bean定义无效或已存在同名的bean定义
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeansException;

    /**
     * 移除一个bean定义
     *
     * @param beanName bean名称
     * @throws BeansException 如果找不到指定名称的bean定义
     */
    void removeBeanDefinition(String beanName) throws BeansException;

    /**
     * 获取bean定义
     *
     * @param beanName bean名称
     * @return bean定义
     * @throws BeansException 如果找不到指定名称的bean定义
     */
    BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    /**
     * 判断是否包含指定名称的bean定义
     *
     * @param beanName bean名称
     * @return 如果包含返回true，否则返回false
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * 获取所有bean定义的名称
     *
     * @return bean定义名称数组
     */
    String[] getBeanDefinitionNames();

    /**
     * 获取bean定义的数量
     *
     * @return bean定义的数量
     */
    int getBeanDefinitionCount();
} 