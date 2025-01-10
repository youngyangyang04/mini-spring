package com.kama.minispring.beans.factory.config;

/**
 * Bean定义接口，描述一个bean的配置信息
 * 
 * @author kama
 * @version 1.0.0
 */
public interface BeanDefinition {
    
    String SCOPE_SINGLETON = "singleton";
    String SCOPE_PROTOTYPE = "prototype";
    
    /**
     * 获取Bean的Class对象
     *
     * @return Bean的Class对象
     */
    Class<?> getBeanClass();
    
    /**
     * 获取Bean的作用域
     *
     * @return Bean的作用域，默认为singleton
     */
    String getScope();
    
    /**
     * 设置Bean的作用域
     *
     * @param scope Bean的作用域
     */
    void setScope(String scope);
    
    /**
     * 判断是否是单例
     *
     * @return 如果是单例返回true，否则返回false
     */
    boolean isSingleton();
    
    /**
     * 判断是否是原型
     *
     * @return 如果是原型返回true，否则返回false
     */
    boolean isPrototype();
    
    /**
     * 获取初始化方法名
     *
     * @return 初始化方法名
     */
    String getInitMethodName();
    
    /**
     * 设置初始化方法名
     *
     * @param initMethodName 初始化方法名
     */
    void setInitMethodName(String initMethodName);
    
    /**
     * 获取销毁方法名
     *
     * @return 销毁方法名
     */
    String getDestroyMethodName();
    
    /**
     * 设置销毁方法名
     *
     * @param destroyMethodName 销毁方法名
     */
    void setDestroyMethodName(String destroyMethodName);
} 