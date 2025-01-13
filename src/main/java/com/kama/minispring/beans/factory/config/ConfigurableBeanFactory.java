package com.kama.minispring.beans.factory.config;

import com.kama.minispring.beans.factory.HierarchicalBeanFactory;

import java.util.List;

/**
 * 可配置的bean工厂接口
 * 提供了配置bean工厂的功能
 *
 * @author kama
 * @version 1.0.0
 */
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

    /**
     * 单例作用域标识符
     */
    String SCOPE_SINGLETON = "singleton";

    /**
     * 原型作用域标识符
     */
    String SCOPE_PROTOTYPE = "prototype";

    /**
     * 设置父bean工厂
     *
     * @param parentBeanFactory 父bean工厂
     */
    void setParentBeanFactory(ConfigurableBeanFactory parentBeanFactory);

    /**
     * 设置类加载器
     *
     * @param beanClassLoader 类加载器
     */
    void setBeanClassLoader(ClassLoader beanClassLoader);

    /**
     * 获取类加载器
     *
     * @return 类加载器
     */
    ClassLoader getBeanClassLoader();

    /**
     * 添加bean后置处理器
     *
     * @param beanPostProcessor bean后置处理器
     */
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

    /**
     * 获取所有bean后置处理器
     *
     * @return bean后置处理器列表
     */
    List<BeanPostProcessor> getBeanPostProcessors();

    /**
     * 获取bean后置处理器的数量
     *
     * @return bean后置处理器的数量
     */
    int getBeanPostProcessorCount();

    /**
     * 注册依赖的bean
     *
     * @param beanName 当前bean的名称
     * @param dependentBeanName 依赖bean的名称
     */
    void registerDependentBean(String beanName, String dependentBeanName);

    /**
     * 获取依赖当前bean的bean名称
     *
     * @param beanName 当前bean的名称
     * @return 依赖的bean名称数组
     */
    String[] getDependentBeans(String beanName);

    /**
     * 获取当前bean依赖的bean名称
     *
     * @param beanName 当前bean的名称
     * @return 被依赖的bean名称数组
     */
    String[] getDependenciesForBean(String beanName);

    /**
     * 销毁所有单例bean
     */
    void destroySingletons();
} 