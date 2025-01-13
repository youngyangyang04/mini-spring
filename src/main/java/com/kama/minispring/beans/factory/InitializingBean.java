package com.kama.minispring.beans.factory;

/**
 * 实现此接口的bean在属性设置完成后会执行afterPropertiesSet方法
 */
public interface InitializingBean {
    /**
     * 在bean的所有属性设置完成后调用
     */
    void afterPropertiesSet() throws Exception;
} 