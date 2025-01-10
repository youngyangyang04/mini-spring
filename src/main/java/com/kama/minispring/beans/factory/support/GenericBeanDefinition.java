package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.factory.config.BeanDefinition;

/**
 * BeanDefinition接口的通用实现类
 * 
 * @author kama
 * @version 1.0.0
 */
public class GenericBeanDefinition implements BeanDefinition {
    
    private Class<?> beanClass;
    private String scope = SCOPE_SINGLETON;
    private String initMethodName;
    private String destroyMethodName;
    
    /**
     * 创建一个GenericBeanDefinition实例
     *
     * @param beanClass bean的Class对象
     */
    public GenericBeanDefinition(Class<?> beanClass) {
        this.beanClass = beanClass;
    }
    
    @Override
    public Class<?> getBeanClass() {
        return this.beanClass;
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
} 