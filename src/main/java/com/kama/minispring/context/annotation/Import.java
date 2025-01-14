package com.kama.minispring.context.annotation;

import java.lang.annotation.*;

/**
 * 用于导入配置类或ImportBeanDefinitionRegistrar实现类的注解
 * 支持导入普通的配置类、ImportSelector的实现类、ImportBeanDefinitionRegistrar的实现类
 *
 * @author kama
 * @version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import {
    
    /**
     * 要导入的类
     * 可以是配置类、ImportSelector实现类或ImportBeanDefinitionRegistrar实现类
     *
     * @return 要导入的类数组
     */
    Class<?>[] value();
} 