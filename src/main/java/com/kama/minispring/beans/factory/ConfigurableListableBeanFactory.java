package com.kama.minispring.beans.factory;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.config.BeanDefinition;
import com.kama.minispring.beans.factory.config.ConfigurableBeanFactory;

import java.util.Map;

/**
 * 可配置的列表化bean工厂接口
 * 提供了配置和列举bean的功能
 *
 * @author kama
 * @version 1.0.0
 */
public interface ConfigurableListableBeanFactory extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

    /**
     * 获取bean定义
     *
     * @param beanName bean名称
     * @return bean定义
     * @throws BeansException 如果找不到bean定义
     */
    BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    /**
     * 预实例化所有单例bean
     *
     * @throws BeansException 如果预实例化过程中发生错误
     */
    void preInstantiateSingletons() throws BeansException;

    /**
     * 确保所有非延迟加载的单例bean都被实例化
     *
     * @throws BeansException 如果实例化过程中发生错误
     */
    void ensureAllSingletonsInstantiated() throws BeansException;
} 