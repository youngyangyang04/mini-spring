package com.kama.minispring.beans.factory.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean定义的包装类，用于保存Bean定义及其构造参数和属性信息
 *
 * @author kama
 * @version 1.0.0
 */
public class BeanDefinitionHolder {
    private final BeanDefinition beanDefinition;
    private final String beanName;
    private final List<ConstructorArgumentValue> constructorArgumentValues;
    private final List<PropertyValue> propertyValues;

    public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
        this.beanDefinition = beanDefinition;
        this.beanName = beanName;
        this.constructorArgumentValues = new ArrayList<>();
        this.propertyValues = new ArrayList<>();
    }

    public BeanDefinition getBeanDefinition() {
        return this.beanDefinition;
    }

    public String getBeanName() {
        return this.beanName;
    }

    public void addConstructorArgumentValue(ConstructorArgumentValue argumentValue) {
        this.constructorArgumentValues.add(argumentValue);
    }

    public List<ConstructorArgumentValue> getConstructorArgumentValues() {
        return new ArrayList<>(this.constructorArgumentValues);
    }

    /**
     * 添加一个属性值
     *
     * @param propertyValue 属性值
     */
    public void addPropertyValue(PropertyValue propertyValue) {
        this.propertyValues.add(propertyValue);
    }

    /**
     * 获取所有属性值
     *
     * @return 属性值列表
     */
    public List<PropertyValue> getPropertyValues() {
        return new ArrayList<>(this.propertyValues);
    }
} 