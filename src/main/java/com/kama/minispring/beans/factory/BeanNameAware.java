package com.kama.minispring.beans.factory;

/**
 * 实现此接口的bean可以感知到自己在容器中的名字
 */
public interface BeanNameAware extends Aware {
    /**
     * 设置bean在容器中的名字
     *
     * @param name bean的名字
     */
    void setBeanName(String name);
} 