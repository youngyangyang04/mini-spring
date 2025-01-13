package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.PropertyValues;
import com.kama.minispring.beans.factory.config.BeanDefinition;
import com.kama.minispring.beans.factory.config.ConstructorArgumentValue;
import com.kama.minispring.beans.factory.config.PropertyValue;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用的bean定义实现类
 */
public class GenericBeanDefinition implements BeanDefinition {
    private Class<?> beanClass;
    private String scope = SCOPE_SINGLETON;
    private String initMethodName;
    private String destroyMethodName;
    private PropertyValues propertyValues;
    private final List<ConstructorArgumentValue> constructorArgumentValues = new ArrayList<>();

    public GenericBeanDefinition(Class<?> beanClass) {
        this.beanClass = beanClass;
        this.propertyValues = new PropertyValues();
    }

    @Override
    public Class<?> getBeanClass() {
        return this.beanClass;
    }

    @Override
    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public String getScope() {
        return this.scope;
    }

    @Override
    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(this.scope);
    }

    @Override
    public boolean isPrototype() {
        return SCOPE_PROTOTYPE.equals(this.scope);
    }

    @Override
    public String getInitMethodName() {
        return this.initMethodName;
    }

    @Override
    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    @Override
    public String getDestroyMethodName() {
        return this.destroyMethodName;
    }

    @Override
    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    @Override
    public PropertyValues getPropertyValues() {
        return this.propertyValues;
    }

    @Override
    public void setPropertyValues(PropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }

    @Override
    public void addPropertyValue(PropertyValue propertyValue) {
        this.propertyValues.addPropertyValue(propertyValue);
    }

    @Override
    public List<ConstructorArgumentValue> getConstructorArgumentValues() {
        return this.constructorArgumentValues;
    }

    @Override
    public void addConstructorArgumentValue(ConstructorArgumentValue constructorArgumentValue) {
        this.constructorArgumentValues.add(constructorArgumentValue);
    }

    @Override
    public boolean hasConstructorArgumentValues() {
        return !this.constructorArgumentValues.isEmpty();
    }
} 