package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.factory.config.PropertyValue;
import com.kama.minispring.beans.factory.config.PropertyValues;
import com.kama.minispring.beans.factory.config.BeanDefinition;
import com.kama.minispring.beans.factory.config.ConstructorArgumentValue;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用的Bean定义实现类
 *
 * @author kama
 * @version 1.0.0
 */
public class GenericBeanDefinition implements BeanDefinition {
    
    private Class<?> beanClass;
    private String scope = SCOPE_SINGLETON;
    private boolean lazyInit = false;
    private String initMethodName;
    private String destroyMethodName;
    private PropertyValues propertyValues = new PropertyValues();
    private final List<ConstructorArgumentValue> constructorArgumentValues = new ArrayList<>();
    
    public GenericBeanDefinition() {
    }
    
    public GenericBeanDefinition(Class<?> beanClass) {
        this.beanClass = beanClass;
    }
    
    @Override
    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }
    
    @Override
    public Class<?> getBeanClass() {
        return this.beanClass;
    }
    
    @Override
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    @Override
    public String getScope() {
        return this.scope;
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
    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }
    
    @Override
    public boolean isLazyInit() {
        return this.lazyInit;
    }
    
    @Override
    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }
    
    @Override
    public String getInitMethodName() {
        return this.initMethodName;
    }
    
    @Override
    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }
    
    @Override
    public String getDestroyMethodName() {
        return this.destroyMethodName;
    }
    
    @Override
    public void setPropertyValues(PropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }
    
    @Override
    public PropertyValues getPropertyValues() {
        return this.propertyValues;
    }
    
    @Override
    public void addPropertyValue(PropertyValue propertyValue) {
        if (this.propertyValues == null) {
            this.propertyValues = new PropertyValues();
        }
        this.propertyValues.addPropertyValue(propertyValue);
    }
    
    @Override
    public void addConstructorArgumentValue(ConstructorArgumentValue constructorArgumentValue) {
        this.constructorArgumentValues.add(constructorArgumentValue);
    }
    
    @Override
    public List<ConstructorArgumentValue> getConstructorArgumentValues() {
        return this.constructorArgumentValues;
    }
    
    @Override
    public boolean hasConstructorArgumentValues() {
        return !this.constructorArgumentValues.isEmpty();
    }
    
    @Override
    public String getBeanClassName() {
        return this.beanClass != null ? this.beanClass.getName() : null;
    }
} 