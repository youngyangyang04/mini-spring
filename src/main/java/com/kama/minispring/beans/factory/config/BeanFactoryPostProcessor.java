package com.kama.minispring.beans.factory.config;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.ConfigurableListableBeanFactory;

/**
 * 允许自定义修改bean定义的接口
 * 在所有bean定义加载完成后，但在bean实例化之前调用
 * 这个接口的实现类可以修改bean的定义信息，比如：
 * 1. 修改bean的属性值
 * 2. 修改bean的作用域
 * 3. 修改bean的依赖关系等
 *
 * @author kama
 * @version 1.0.0
 */
public interface BeanFactoryPostProcessor {

    /**
     * 在所有bean定义加载完成后，但在bean实例化之前，
     * 允许修改bean的定义信息
     *
     * @param beanFactory 容器
     * @throws BeansException 如果处理过程中发生错误
     */
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;
} 