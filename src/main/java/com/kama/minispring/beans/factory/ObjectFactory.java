package com.kama.minispring.beans.factory;

import com.kama.minispring.beans.BeansException;

/**
 * 定义一个对象工厂接口，用于创建bean实例
 *
 * @author kama
 * @version 1.0.0
 */
@FunctionalInterface
public interface ObjectFactory<T> {
    /**
     * 获取对象实例
     *
     * @return 对象实例
     * @throws BeansException 如果获取对象失败
     */
    T getObject() throws BeansException;
} 