package com.kama.minispring.beans.factory.annotation;

import java.lang.annotation.*;

/**
 * 标记需要自动注入的字段或方法的注解
 * 用于依赖注入，类似于Spring的@Autowired注解
 *
 * @author kama
 * @version 1.0.0
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    
    /**
     * 声明该依赖是否是必需的
     * 如果为true且找不到依赖的bean，则会抛出异常
     * 如果为false，找不到依赖的bean时会跳过注入
     *
     * @return 依赖是否是必需的
     */
    boolean required() default true;
} 