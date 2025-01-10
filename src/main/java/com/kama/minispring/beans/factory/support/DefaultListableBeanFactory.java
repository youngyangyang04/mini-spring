package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.BeanFactory;
import com.kama.minispring.beans.factory.ObjectFactory;
import com.kama.minispring.beans.factory.config.BeanDefinition;
import com.kama.minispring.beans.factory.config.BeanDefinitionHolder;
import com.kama.minispring.beans.factory.config.ConstructorArgumentValue;
import com.kama.minispring.beans.factory.config.PropertyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * BeanFactory接口的默认实现
 * 基于列表的bean工厂实现，支持单例bean的注册、别名机制、依赖注入和循环依赖处理
 *
 * @author kama
 * @version 1.0.0
 */
public class DefaultListableBeanFactory extends SimpleAliasRegistry implements BeanFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultListableBeanFactory.class);
    
    /** 一级缓存：存储完全初始化好的单例bean */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    
    /** 二级缓存：存储早期的单例bean对象（未完全初始化） */
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);
    
    /** 三级缓存：存储单例bean的工厂对象 */
    private final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>(16);
    
    /** 存储当前正在创建的bean名称 */
    private final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<>(16));
    
    /** 存储bean定义的容器 */
    private final Map<String, BeanDefinitionHolder> beanDefinitionMap = new ConcurrentHashMap<>(256);
    
    @Override
    public Object getBean(String name) throws BeansException {
        String beanName = canonicalName(name);
        BeanDefinitionHolder holder = getBeanDefinitionHolder(beanName);
        BeanDefinition beanDefinition = holder.getBeanDefinition();
        
        if (beanDefinition.isSingleton()) {
            // 首先检查一级缓存
            Object singleton = singletonObjects.get(beanName);
            if (singleton != null) {
                logger.debug("Found singleton bean '{}' in primary cache", beanName);
                return singleton;
            }
            
            // 检查是否存在循环依赖
            singleton = getSingletonEarly(beanName);
            if (singleton != null) {
                return singleton;
            }
            
            // 标记bean正在创建中
            beforeSingletonCreation(beanName);
            try {
                // 创建bean实例
                singleton = createBean(beanName, holder);
                // 将完全初始化的bean放入一级缓存
                addSingleton(beanName, singleton);
            } finally {
                // 移除创建中的标记
                afterSingletonCreation(beanName);
            }
            return singleton;
        } else {
            // 如果是原型bean，每次都创建新实例
            Object prototype = createBean(beanName, holder);
            logger.debug("Instantiated prototype bean '{}'", beanName);
            return prototype;
        }
    }
    
    /**
     * 获取早期的单例bean（用于处理循环依赖）
     */
    protected Object getSingletonEarly(String beanName) {
        // 检查二级缓存
        Object singleton = earlySingletonObjects.get(beanName);
        if (singleton == null && isSingletonCurrentlyInCreation(beanName)) {
            synchronized (this.singletonObjects) {
                // 再次检查二级缓存
                singleton = earlySingletonObjects.get(beanName);
                if (singleton == null) {
                    // 检查三级缓存
                    ObjectFactory<?> factory = singletonFactories.get(beanName);
                    if (factory != null) {
                        // 从工厂获取对象并放入二级缓存
                        singleton = factory.getObject();
                        earlySingletonObjects.put(beanName, singleton);
                        singletonFactories.remove(beanName);
                        logger.debug("Created early reference for singleton bean '{}'", beanName);
                    }
                }
            }
        }
        return singleton;
    }
    
    /**
     * 创建bean实例
     */
    protected Object createBean(String name, BeanDefinitionHolder holder) throws BeansException {
        BeanDefinition beanDefinition = holder.getBeanDefinition();
        Class<?> beanClass = beanDefinition.getBeanClass();
        Object bean;
        
        try {
            // 处理构造器注入
            List<ConstructorArgumentValue> constructorArgs = holder.getConstructorArgumentValues();
            if (!constructorArgs.isEmpty()) {
                // 使用带参数的构造器创建实例
                Class<?>[] parameterTypes = new Class<?>[constructorArgs.size()];
                Object[] parameterValues = new Object[constructorArgs.size()];
                
                for (int i = 0; i < constructorArgs.size(); i++) {
                    ConstructorArgumentValue argumentValue = constructorArgs.get(i);
                    parameterTypes[i] = argumentValue.getType();
                    parameterValues[i] = getBean(argumentValue.getValue().toString());
                }
                
                Constructor<?> constructor = beanClass.getDeclaredConstructor(parameterTypes);
                bean = constructor.newInstance(parameterValues);
                logger.debug("Created bean '{}' using constructor injection", name);
            } else {
                // 使用默认构造器创建实例
                bean = beanClass.getDeclaredConstructor().newInstance();
                logger.debug("Created bean '{}' using default constructor", name);
            }
            
            // 如果是单例且正在创建中（可能存在循环依赖），将工厂放入三级缓存
            if (beanDefinition.isSingleton() && isSingletonCurrentlyInCreation(name)) {
                singletonFactories.put(name, () -> bean);
                logger.debug("Added factory for singleton bean '{}' to third-level cache", name);
            }
            
            // 处理setter注入
            List<PropertyValue> propertyValues = holder.getPropertyValues();
            if (!propertyValues.isEmpty()) {
                for (PropertyValue propertyValue : propertyValues) {
                    String propertyName = propertyValue.getName();
                    Object value = propertyValue.getValue();
                    
                    // 如果属性值是引用其他bean，则获取对应的bean实例
                    if (value instanceof String && containsBean(value.toString())) {
                        value = getBean(value.toString());
                    }
                    
                    // 使用PropertyDescriptor进行属性注入
                    PropertyDescriptor pd = new PropertyDescriptor(propertyName, beanClass);
                    Method writeMethod = pd.getWriteMethod();
                    if (writeMethod != null) {
                        writeMethod.invoke(bean, value);
                        logger.debug("Injected property '{}' of bean '{}'", propertyName, name);
                    }
                }
            }
            
            // 调用初始化方法
            String initMethodName = beanDefinition.getInitMethodName();
            if (initMethodName != null && !initMethodName.isEmpty()) {
                Method initMethod = beanClass.getMethod(initMethodName);
                initMethod.invoke(bean);
                logger.debug("Invoked init-method '{}' of bean '{}'", initMethodName, name);
            }
            
        } catch (Exception e) {
            throw new BeansException("Error creating bean with name '" + name + "'", e);
        }
        return bean;
    }
    
    /**
     * 将bean标记为正在创建中
     */
    protected void beforeSingletonCreation(String beanName) {
        if (!singletonsCurrentlyInCreation.add(beanName)) {
            throw new BeansException(
                "Circular reference detected during bean creation for singleton '" + beanName + "'");
        }
    }
    
    /**
     * 移除bean的创建中标记
     */
    protected void afterSingletonCreation(String beanName) {
        if (!singletonsCurrentlyInCreation.remove(beanName)) {
            throw new BeansException(
                "Singleton '" + beanName + "' isn't currently in creation");
        }
    }
    
    /**
     * 检查bean是否正在创建中
     */
    protected boolean isSingletonCurrentlyInCreation(String beanName) {
        return singletonsCurrentlyInCreation.contains(beanName);
    }
    
    /**
     * 将完全初始化的bean添加到一级缓存
     */
    protected void addSingleton(String beanName, Object singleton) {
        synchronized (this.singletonObjects) {
            singletonObjects.put(beanName, singleton);
            earlySingletonObjects.remove(beanName);
            singletonFactories.remove(beanName);
            logger.debug("Added singleton bean '{}' to primary cache", beanName);
        }
    }
    
    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        Object bean = getBean(name);
        if (requiredType != null && !requiredType.isInstance(bean)) {
            throw new BeansException(
                "Bean named '" + name + "' is expected to be of type '" + requiredType.getName() +
                "' but was actually of type '" + bean.getClass().getName() + "'");
        }
        return requiredType.cast(bean);
    }
    
    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        // 遍历beanDefinitionMap，查找类型匹配的bean
        for (Map.Entry<String, BeanDefinitionHolder> entry : beanDefinitionMap.entrySet()) {
            if (requiredType.isAssignableFrom(entry.getValue().getBeanDefinition().getBeanClass())) {
                return requiredType.cast(getBean(entry.getKey()));
            }
        }
        throw new BeansException("No qualifying bean of type '" + requiredType.getName() + "' available");
    }
    
    @Override
    public boolean containsBean(String name) {
        String beanName = canonicalName(name);
        return beanDefinitionMap.containsKey(beanName);
    }
    
    @Override
    public boolean isSingleton(String name) throws BeansException {
        String beanName = canonicalName(name);
        BeanDefinition beanDefinition = getBeanDefinitionHolder(beanName).getBeanDefinition();
        return beanDefinition.isSingleton();
    }
    
    @Override
    public boolean isPrototype(String name) throws BeansException {
        String beanName = canonicalName(name);
        BeanDefinition beanDefinition = getBeanDefinitionHolder(beanName).getBeanDefinition();
        return beanDefinition.isPrototype();
    }
    
    /**
     * 获取bean定义持有者
     *
     * @param name bean的名称
     * @return bean定义持有者
     * @throws BeansException 如果找不到bean定义
     */
    public BeanDefinitionHolder getBeanDefinitionHolder(String name) throws BeansException {
        String beanName = canonicalName(name);
        BeanDefinitionHolder holder = beanDefinitionMap.get(beanName);
        if (holder == null) {
            throw new BeansException("No bean named '" + name + "' is defined");
        }
        return holder;
    }
    
    /**
     * 注册一个bean定义
     *
     * @param name bean的名称
     * @param beanDefinition bean的定义
     */
    public void registerBeanDefinition(String name, BeanDefinition beanDefinition) {
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, name);
        beanDefinitionMap.put(name, holder);
        logger.debug("Registered bean definition for bean named '{}'", name);
    }
    
    /**
     * 注册一个单例bean
     *
     * @param name bean的名称
     * @param singletonObject bean实例
     */
    public void registerSingleton(String name, Object singletonObject) {
        String beanName = canonicalName(name);
        singletonObjects.put(beanName, singletonObject);
        logger.debug("Registered singleton bean named '{}'", beanName);
    }
    
    /**
     * 销毁所有单例bean
     */
    public void destroySingletons() {
        for (Map.Entry<String, Object> entry : singletonObjects.entrySet()) {
            String beanName = entry.getKey();
            Object bean = entry.getValue();
            BeanDefinitionHolder holder = beanDefinitionMap.get(beanName);
            
            if (holder != null) {
                BeanDefinition beanDefinition = holder.getBeanDefinition();
                String destroyMethodName = beanDefinition.getDestroyMethodName();
                if (destroyMethodName != null && !destroyMethodName.isEmpty()) {
                    try {
                        Method destroyMethod = bean.getClass().getMethod(destroyMethodName);
                        destroyMethod.invoke(bean);
                        logger.debug("Invoked destroy-method '{}' of bean '{}'", destroyMethodName, beanName);
                    } catch (Exception e) {
                        logger.error("Error invoking destroy-method '{}' of bean '{}'", destroyMethodName, beanName, e);
                    }
                }
            }
        }
        singletonObjects.clear();
        logger.debug("Destroyed all singleton beans");
    }
} 