package com.kama.minispring.beans.factory.config;

import com.kama.minispring.beans.BeansException;

/**
 * Bean后置处理器接口
 * 允许自定义修改bean实例的工厂钩子
 *
 * @author kama
 * @version 1.0.0
 */
public interface BeanPostProcessor {

    /**
     * 在bean初始化之前应用此BeanPostProcessor
     *
     * @param bean bean实例
     * @param beanName bean名称
     * @return 要使用的bean实例，可以是原始的或包装过的
     * @throws BeansException 在处理过程中发生错误
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 在bean初始化之后应用此BeanPostProcessor
     *
     * @param bean bean实例
     * @param beanName bean名称
     * @return 要使用的bean实例，可以是原始的或包装过的
     * @throws BeansException 在处理过程中发生错误
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
} 