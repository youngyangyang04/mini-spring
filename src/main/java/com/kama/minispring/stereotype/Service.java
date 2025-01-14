package com.kama.minispring.stereotype;

import java.lang.annotation.*;

/**
 * 标记服务层组件的注解
 * 被此注解标记的类将被自动注册为Spring Bean
 *
 * @author kama
 * @version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {
    
    /**
     * Bean的名称，默认为空
     * 如果未指定，将使用类名的首字母小写形式作为Bean名称
     *
     * @return Bean的名称
     */
    String value() default "";
} 