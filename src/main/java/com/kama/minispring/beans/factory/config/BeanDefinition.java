package com.kama.minispring.beans.factory.config;

import com.kama.minispring.beans.PropertyValues;
import java.util.List;

/**
 * Bean定义接口
 * 定义了获取bean定义信息的方法
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
     * 获取bean的类型
     *
     * @return bean的类型
     */
    Class<?> getBeanClass();

    /**
     * 设置bean的类型
     *
     * @param beanClass bean的类型
     */
    void setBeanClass(Class<?> beanClass);

    /**
     * 获取bean的作用域
     *
     * @return bean的作用域
     */
    String getScope();

    /**
     * 设置bean的作用域
     *
     * @param scope bean的作用域
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
     * 获取bean的初始化方法名
     *
     * @return 初始化方法名
     */
    String getInitMethodName();

    /**
     * 设置bean的初始化方法名
     *
     * @param initMethodName 初始化方法名
     */
    void setInitMethodName(String initMethodName);

    /**
     * 获取bean的销毁方法名
     *
     * @return 销毁方法名
     */
    String getDestroyMethodName();

    /**
     * 设置bean的销毁方法名
     *
     * @param destroyMethodName 销毁方法名
     */
    void setDestroyMethodName(String destroyMethodName);

    /**
     * 获取构造函数参数值列表
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
     * 获取属性值列表
     *
     * @return 属性值列表
     */
    PropertyValues getPropertyValues();

    /**
     * 设置属性值列表
     *
     * @param propertyValues 属性值列表
     */
    void setPropertyValues(PropertyValues propertyValues);

    /**
     * 添加属性值
     * @param propertyValue 属性值
     */
    void addPropertyValue(PropertyValue propertyValue);
} 