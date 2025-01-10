package com.kama.minispring.beans.factory;

import com.kama.minispring.beans.BeansException;

/**
 * 对象工厂接口，用于延迟创建对象
 * 主要用于解决循环依赖问题
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
     * @throws BeansException 如果创建对象失败
     */
    T getObject() throws BeansException;
} 