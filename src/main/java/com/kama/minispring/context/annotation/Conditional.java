package com.kama.minispring.context.annotation;

import java.lang.annotation.*;

/**
 * 条件注解，用于根据特定条件决定是否创建Bean
 * 
 * @author kama
 * @version 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conditional {
    
    /**
     * 条件类数组
     * 所有条件都满足时才会创建Bean
     *
     * @return 条件类数组
     */
    Class<? extends Condition>[] value();
} 