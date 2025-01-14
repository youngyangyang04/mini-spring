package com.kama.minispring.context.annotation;


import com.kama.minispring.beans.factory.support.BeanDefinitionRegistry;
import com.kama.minispring.core.env.Environment;
import com.kama.minispring.core.io.ResourceLoader;

/**
 * 条件上下文接口，提供条件评估所需的上下文信息
 * 
 * @author kama
 * @version 1.0.0
 */
public interface ConditionContext {
    
    /**
     * 获取Bean定义注册表
     *
     * @return Bean定义注册表
     */
    BeanDefinitionRegistry getRegistry();
    
    /**
     * 获取类加载器
     *
     * @return 类加载器
     */
    ClassLoader getClassLoader();
    
    /**
     * 获取环境配置
     *
     * @return 环境配置
     */
    Environment getEnvironment();
    
    /**
     * 获取资源加载器
     *
     * @return 资源加载器
     */
    ResourceLoader getResourceLoader();
} 