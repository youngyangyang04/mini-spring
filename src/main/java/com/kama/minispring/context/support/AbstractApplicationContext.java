package com.kama.minispring.context.support;

import com.kama.minispring.beans.factory.BeanFactory;
import com.kama.minispring.beans.factory.config.ConfigurableListableBeanFactory;
import com.kama.minispring.context.ApplicationContext;
import com.kama.minispring.core.io.DefaultResourceLoader;
import com.kama.minispring.core.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ApplicationContext接口的抽象实现
 * 提供了上下文的基础功能实现
 *
 * @author kama
 * @version 1.0.0
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ApplicationContext {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractApplicationContext.class);
    
    private final long startupDate;
    private final AtomicBoolean active = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();
    private ApplicationContext parent;
    private String id;
    private String displayName;
    
    public AbstractApplicationContext() {
        this(null);
    }
    
    public AbstractApplicationContext(ApplicationContext parent) {
        this.parent = parent;
        this.startupDate = System.currentTimeMillis();
    }
    
    @Override
    public String getId() {
        return this.id;
    }
    
    @Override
    public String getDisplayName() {
        return this.displayName;
    }
    
    @Override
    public long getStartupDate() {
        return this.startupDate;
    }
    
    @Override
    public ApplicationContext getParent() {
        return this.parent;
    }
    
    /**
     * 刷新应用上下文
     * 这是一个模板方法，定义了上下文刷新的整体流程
     */
    public void refresh() throws Exception {
        synchronized (this) {
            // 准备刷新上下文
            prepareRefresh();
            
            // 获取bean工厂
            ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
            
            // 准备bean工厂
            prepareBeanFactory(beanFactory);
            
            try {
                // 允许在上下文子类中对bean工厂进行后处理
                postProcessBeanFactory(beanFactory);
                
                // 调用BeanFactoryPostProcessor
                invokeBeanFactoryPostProcessors(beanFactory);
                
                // 注册BeanPostProcessor
                registerBeanPostProcessors(beanFactory);
                
                // 初始化消息源
                initMessageSource();
                
                // 初始化事件多播器
                initApplicationEventMulticaster();
                
                // 初始化其他特殊bean
                onRefresh();
                
                // 注册监听器
                registerListeners();
                
                // 完成bean工厂的初始化
                finishBeanFactoryInitialization(beanFactory);
                
                // 完成刷新
                finishRefresh();
            } catch (Exception ex) {
                logger.error("Context refresh failed", ex);
                throw ex;
            }
        }
    }
    
    protected void prepareRefresh() {
        this.active.set(true);
        this.closed.set(false);
        logger.info("Refreshing " + getDisplayName());
    }
    
    protected abstract ConfigurableListableBeanFactory obtainFreshBeanFactory();
    
    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // 设置类加载器
        beanFactory.setBeanClassLoader(getClassLoader());
    }
    
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // 默认实现为空，留给子类扩展
    }
    
    protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        // 执行BeanFactoryPostProcessor
    }
    
    protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        // 注册BeanPostProcessor
    }
    
    protected void initMessageSource() {
        // 初始化消息源
    }
    
    protected void initApplicationEventMulticaster() {
        // 初始化事件多播器
    }
    
    protected void onRefresh() {
        // 留给子类实现特殊bean的初始化
    }
    
    protected void registerListeners() {
        // 注册监听器
    }
    
    protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
        // 初始化所有剩余的单例bean
    }
    
    protected void finishRefresh() {
        // 完成刷新，发布上下文刷新事件
    }
    
    /**
     * 设置上下文ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * 设置显示名称
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    @Override
    public Object getBean(String name) {
        return getBeanFactory().getBean(name);
    }
    
    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        return getBeanFactory().getBean(name, requiredType);
    }
    
    @Override
    public boolean containsBean(String name) {
        return getBeanFactory().containsBean(name);
    }
    
    @Override
    public boolean isSingleton(String name) {
        return getBeanFactory().isSingleton(name);
    }
    
    @Override
    public boolean isPrototype(String name) {
        return getBeanFactory().isPrototype(name);
    }
    
    @Override
    public Class<?> getType(String name) {
        return getBeanFactory().getType(name);
    }
    
    /**
     * 获取内部的bean工厂
     */
    protected abstract BeanFactory getBeanFactory();
} 