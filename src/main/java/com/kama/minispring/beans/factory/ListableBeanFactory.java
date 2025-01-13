package com.kama.minispring.beans.factory;

import com.kama.minispring.beans.BeansException;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 可列表化的bean工厂接口
 * 提供了枚举bean的功能
 *
 * @author kama
 * @version 1.0.0
 */
public interface ListableBeanFactory extends BeanFactory {

    /**
     * 判断是否包含指定名称的bean定义
     *
     * @param beanName bean名称
     * @return 如果包含返回true，否则返回false
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * 获取bean定义的数量
     *
     * @return bean定义的数量
     */
    int getBeanDefinitionCount();

    /**
     * 获取所有bean定义的名称
     *
     * @return bean定义名称数组
     */
    String[] getBeanDefinitionNames();

    /**
     * 根据类型获取bean的名称
     *
     * @param type bean类型
     * @return bean名称数组
     */
    String[] getBeanNamesForType(Class<?> type);

    /**
     * 根据类型获取bean实例
     *
     * @param type bean类型
     * @return bean实例的Map，key为bean名称，value为bean实例
     * @throws BeansException 如果获取bean失败
     */
    <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;

    /**
     * 获取带有指定注解的bean
     *
     * @param annotationType 注解类型
     * @return bean实例的Map，key为bean名称，value为bean实例
     * @throws BeansException 如果获取bean失败
     */
    Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;

    /**
     * 获取指定bean上的注解
     *
     * @param beanName bean名称
     * @param annotationType 注解类型
     * @return 注解实例，如果不存在返回null
     */
    <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws BeansException;
} 