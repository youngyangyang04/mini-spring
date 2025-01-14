package com.kama.minispring.beans.factory.config;

import com.kama.minispring.beans.BeansException;


/**
 * BeanPostProcessor接口的扩展，允许在实例化前后进行干预
 * 主要用于处理bean的实例化、属性设置等生命周期阶段
 *
 * @author kama
 * @version 1.0.0
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

    /**
     * 在bean实例化之前应用此BeanPostProcessor
     * 返回的对象会替换原本的bean，如果返回null则继续正常的实例化流程
     *
     * @param beanClass bean的Class对象
     * @param beanName bean的名称
     * @return 如果要替换掉原本的bean则返回一个对象，否则返回null
     * @throws BeansException 如果处理过程中发生错误
     */
    Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException;

    /**
     * 在bean实例化之后，但在设置属性之前应用此BeanPostProcessor
     * 返回false会阻止后续的属性设置
     *
     * @param bean bean实例
     * @param beanName bean的名称
     * @return 是否继续处理属性设置
     * @throws BeansException 如果处理过程中发生错误
     */
    boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException;

    /**
     * 在bean的属性被设置之前对属性值进行处理
     * 可以修改属性值或者添加新的属性
     *
     * @param pvs 属性值对象
     * @param bean bean实例
     * @param beanName bean的名称
     * @return 处理后的属性值对象
     * @throws BeansException 如果处理过程中发生错误
     */
    PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName) throws BeansException;
} 