package com.kama.minispring.beans.factory;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.config.BeanPostProcessor;
import com.kama.minispring.beans.factory.config.ConfigurableBeanFactory;

/**
 * 提供自动装配能力的bean工厂接口
 *
 * @author kama
 * @version 1.0.0
 */
public interface AutowireCapableBeanFactory extends BeanFactory {

    /**
     * 不进行自动装配
     */
    int AUTOWIRE_NO = 0;

    /**
     * 通过名称自动装配
     */
    int AUTOWIRE_BY_NAME = 1;

    /**
     * 通过类型自动装配
     */
    int AUTOWIRE_BY_TYPE = 2;

    /**
     * 通过构造函数自动装配
     */
    int AUTOWIRE_CONSTRUCTOR = 3;

    /**
     * 创建一个新的bean实例
     *
     * @param beanClass bean的类型
     * @return 新创建的bean实例
     * @throws BeansException 如果创建失败
     */
    <T> T createBean(Class<T> beanClass) throws BeansException;

    /**
     * 自动装配指定的bean
     *
     * @param existingBean 已存在的bean实例
     * @param autowireMode 自动装配模式
     * @throws BeansException 如果自动装配失败
     */
    void autowireBean(Object existingBean) throws BeansException;

    /**
     * 配置给定的bean实例
     * 应用bean后置处理器、初始化方法等
     *
     * @param existingBean 已存在的bean实例
     * @return 配置后的bean实例
     * @throws BeansException 如果配置失败
     */
    Object configureBean(Object existingBean, String beanName) throws BeansException;

    /**
     * 解析指定bean的依赖
     *
     * @param descriptor bean的描述符
     * @param beanName bean的名称
     * @param autowiredBeanNames 用于存储自动装配的bean名称
     * @return 解析后的值
     * @throws BeansException 如果解析失败
     */
    Object resolveDependency(Class<?> descriptor, String beanName) throws BeansException;

    /**
     * 获取bean工厂
     *
     * @return bean工厂
     */
    ConfigurableBeanFactory getBeanFactory();
} 