package com.kama.minispring.context.annotation;


import com.kama.minispring.beans.factory.support.BeanDefinitionRegistry;
import com.kama.minispring.core.env.Environment;
import com.kama.minispring.core.io.ResourceLoader;
import com.kama.minispring.util.ClassUtils;

/**
 * 标准条件上下文实现类
 * 
 * @author kama
 * @version 1.0.0
 */
public class StandardConditionContext implements ConditionContext {
    
    private final BeanDefinitionRegistry registry;
    private final Environment environment;
    private final ResourceLoader resourceLoader;
    private final ClassLoader classLoader;
    
    public StandardConditionContext(BeanDefinitionRegistry registry, Environment environment,
                                  ResourceLoader resourceLoader) {
        this.registry = registry;
        this.environment = environment;
        this.resourceLoader = resourceLoader;
        this.classLoader = ClassUtils.getDefaultClassLoader();
    }
    
    @Override
    public BeanDefinitionRegistry getRegistry() {
        return this.registry;
    }
    
    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
    
    @Override
    public Environment getEnvironment() {
        return this.environment;
    }
    
    @Override
    public ResourceLoader getResourceLoader() {
        return this.resourceLoader;
    }
} 