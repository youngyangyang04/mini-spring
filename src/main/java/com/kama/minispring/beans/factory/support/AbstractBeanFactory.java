package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.AutowireCapableBeanFactory;
import com.kama.minispring.beans.factory.config.BeanDefinition;
import com.kama.minispring.beans.factory.config.ConfigurableBeanFactory;
import com.kama.minispring.beans.factory.config.BeanPostProcessor;
import com.kama.minispring.beans.factory.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.Map;

/**
 * BeanFactory的抽象实现
 * 定义了获取bean的基本流程
 *
 * @author kama
 * @version 1.0.0
 */
public abstract class AbstractBeanFactory extends SimpleAliasRegistry implements ConfigurableBeanFactory, AutowireCapableBeanFactory {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBeanFactory.class);
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    private ClassLoader beanClassLoader = Thread.currentThread().getContextClassLoader();
    private ConfigurableBeanFactory parentBeanFactory;
    /** 当前正在创建的bean名称的集合 */
    protected final Set<String> singletonsCurrentlyInCreation =
            Collections.newSetFromMap(new ConcurrentHashMap<>(16));
    private final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>(16);
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /**
     * 在单例创建之前调用
     */
    protected void beforeSingletonCreation(String beanName) {
        if (!this.singletonsCurrentlyInCreation.add(beanName)) {
            throw new BeansException("Bean with name '" + beanName + "' is currently in creation");
        }
    }

    /**
     * 在单例创建之后调用
     */
    protected void afterSingletonCreation(String beanName) {
        if (!this.singletonsCurrentlyInCreation.remove(beanName)) {
            throw new BeansException("Bean with name '" + beanName + "' is not in creation");
        }
    }

    /**
     * 添加单例工厂
     */
    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, singletonFactory);
                this.earlySingletonObjects.remove(beanName);
            }
        }
    }

    /**
     * 注册单例对象
     */
    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            logger.debug("Registered singleton bean named '{}'", beanName);
        }
    }

    /**
     * 添加单例对象
     */
    public void addSingleton(String beanName, Object singletonObject) {
        registerSingleton(beanName, singletonObject);
    }

    /**
     * 获取单例对象
     */
    @Override
    public Object getSingleton(String beanName) {
        // 首先从单例缓存中获取
        Object singletonObject = this.singletonObjects.get(beanName);
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            synchronized (this.singletonObjects) {
                // 从早期单例缓存中获取
                singletonObject = this.earlySingletonObjects.get(beanName);
                if (singletonObject == null) {
                    // 从单例工厂中获取
                    ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        singletonObject = singletonFactory.getObject();
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        this.singletonFactories.remove(beanName);
                    }
                }
            }
        }
        return singletonObject;
    }

    /**
     * 判断bean是否正在创建中
     */
    protected boolean isSingletonCurrentlyInCreation(String beanName) {
        return this.singletonsCurrentlyInCreation.contains(beanName);
    }

    /**
     * 获取早期bean引用
     */
    protected Object getEarlyBeanReference(String beanName, BeanDefinition beanDefinition, Object bean) {
        return bean;
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return doGetBean(name, null);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return doGetBean(name, requiredType);
    }

    @SuppressWarnings("unchecked")
    protected <T> T doGetBean(String name, Class<T> requiredType) {
        String beanName = transformedBeanName(name);
        Object bean;

        // 获取bean定义
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        if (beanDefinition == null) {
            throw new BeansException("No bean named '" + beanName + "' is defined");
        }

        // 根据作用域处理
        if (beanDefinition.isSingleton()) {
            // 对于单例bean，尝试从缓存获取
            bean = getSingleton(beanName);
            if (bean == null) {
                bean = createBean(beanName, beanDefinition);
                addSingleton(beanName, bean);
            }
        } else if (beanDefinition.isPrototype()) {
            // 对于原型bean，每次都创建新实例
            bean = createBean(beanName, beanDefinition);
        } else {
            throw new BeansException("Unsupported scope '" + beanDefinition.getScope() + "' for bean '" + beanName + "'");
        }

        // 类型检查
        if (requiredType != null && !requiredType.isInstance(bean)) {
            throw new BeansException("Bean named '" + name + "' is expected to be of type '" + requiredType + "' but was actually of type '" + bean.getClass().getName() + "'");
        }

        return (T) bean;
    }

    protected abstract Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException;

    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.beanClassLoader = (beanClassLoader != null ? beanClassLoader : Thread.currentThread().getContextClassLoader());
    }

    @Override
    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

    @Override
    public void setParentBeanFactory(ConfigurableBeanFactory parentBeanFactory) {
        if (this.parentBeanFactory != null) {
            throw new IllegalStateException("Already has a parent BeanFactory");
        }
        this.parentBeanFactory = parentBeanFactory;
    }

    @Override
    public ConfigurableBeanFactory getParentBeanFactory() {
        return this.parentBeanFactory;
    }

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
        logger.debug("Added bean post processor: {}", beanPostProcessor);
    }

    @Override
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    /**
     * 转换bean名称
     * 处理别名等情况
     */
    protected String transformedBeanName(String name) {
        return canonicalName(name);
    }

    /**
     * 获取bean的模板方法
     */
    protected abstract Object doGetBean(String beanName) throws BeansException;

    /**
     * 在初始化之前应用BeanPostProcessor
     */
    protected abstract Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
            throws BeansException;

    /**
     * 在初始化之后应用BeanPostProcessor
     */
    protected abstract Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
            throws BeansException;
} 