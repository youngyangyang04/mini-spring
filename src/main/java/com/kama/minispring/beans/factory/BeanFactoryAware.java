package com.kama.minispring.beans.factory;

/**
 * 实现此接口的bean可以感知到所属的BeanFactory
 */
public interface BeanFactoryAware extends Aware {
    /**
     * 设置所属的BeanFactory
     *
     * @param beanFactory 所属的BeanFactory
     */
    void setBeanFactory(BeanFactory beanFactory);
} 