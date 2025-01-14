package com.kama.minispring.beans.factory.config;

import java.util.List;

/**
 * Bean定义接口
 * 定义一个Bean的所有属性
 *
 * @author kama
 * @version 1.0.0
 */
public interface BeanDefinition {

    /**
     * 单例作用域
     */
    String SCOPE_SINGLETON = "singleton";

    /**
     * 原型作用域
     */
    String SCOPE_PROTOTYPE = "prototype";

    /**
     * 设置Bean的Class对象
     *
     * @param beanClass Bean的Class对象
     */
    void setBeanClass(Class<?> beanClass);

    /**
     * 获取Bean的Class对象
     *
     * @return Bean的Class对象
     */
    Class<?> getBeanClass();

    /**
     * 设置Bean的作用域
     *
     * @param scope 作用域
     */
    void setScope(String scope);

    /**
     * 获取Bean的作用域
     *
     * @return 作用域
     */
    String getScope();

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
     * 设置是否延迟初始化
     *
     * @param lazyInit 是否延迟初始化
     */
    void setLazyInit(boolean lazyInit);

    /**
     * 是否延迟初始化
     *
     * @return 如果延迟初始化返回true，否则返回false
     */
    boolean isLazyInit();

    /**
     * 设置初始化方法名
     *
     * @param initMethodName 初始化方法名
     */
    void setInitMethodName(String initMethodName);

    /**
     * 获取初始化方法名
     *
     * @return 初始化方法名
     */
    String getInitMethodName();

    /**
     * 设置销毁方法名
     *
     * @param destroyMethodName 销毁方法名
     */
    void setDestroyMethodName(String destroyMethodName);

    /**
     * 获取销毁方法名
     *
     * @return 销毁方法名
     */
    String getDestroyMethodName();

    /**
     * 设置属性值
     *
     * @param propertyValues 属性值对象
     */
    void setPropertyValues(PropertyValues propertyValues);

    /**
     * 获取属性值
     *
     * @return 属性值对象
     */
    PropertyValues getPropertyValues();

    /**
     * 添加属性值
     *
     * @param propertyValue 属性值
     */
    void addPropertyValue(PropertyValue propertyValue);

    /**
     * 获取构造函数参数值
     *
     * @return 构造函数参数值列表
     */
    List<ConstructorArgumentValue> getConstructorArgumentValues();

    /**
     * 添加构造函数参数值
     *
     * @param constructorArgumentValue 构造函数参数值
     */
    void addConstructorArgumentValue(ConstructorArgumentValue constructorArgumentValue);

    /**
     * 是否有构造函数参数
     *
     * @return 如果有构造函数参数返回true，否则返回false
     */
    boolean hasConstructorArgumentValues();

    /**
     * 获取Bean的类名
     *
     * @return Bean的类名
     */
    String getBeanClassName();
} 