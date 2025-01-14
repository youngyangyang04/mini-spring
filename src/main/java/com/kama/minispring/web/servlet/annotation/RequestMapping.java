package com.kama.minispring.web.servlet.annotation;

import java.lang.annotation.*;

/**
 * 请求映射注解
 * 用于标注处理器方法，指定请求路径和方法
 *
 * @author kama
 * @version 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    
    /**
     * 请求路径
     */
    String value() default "";
    
    /**
     * 请求方法
     */
    RequestMethod[] method() default {};
} 