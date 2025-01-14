package com.kama.minispring.context.annotation;

import com.kama.minispring.beans.factory.support.BeanDefinitionRegistry;
import com.kama.minispring.core.type.AnnotationMetadata;

/**
 * 允许在运行时注册额外的Bean定义的接口
 * 通常与@Import注解一起使用，用于注册额外的Bean
 *
 * @author kama
 * @version 1.0.0
 */
public interface ImportBeanDefinitionRegistrar {
    
    /**
     * 根据导入的@Configuration类的注解信息向容器注册Bean定义
     *
     * @param importingClassMetadata 导入类的注解元数据
     * @param registry Bean定义注册表
     */
    void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, 
                               BeanDefinitionRegistry registry);
} 