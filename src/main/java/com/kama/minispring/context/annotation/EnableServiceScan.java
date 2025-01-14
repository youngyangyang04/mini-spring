package com.kama.minispring.context.annotation;

import java.lang.annotation.*;

/**
 * 启用服务类扫描的注解
 * 使用此注解可以自动扫描指定包下的@Service注解标记的类
 *
 * @author kama
 * @version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ServiceScanRegistrar.class)
public @interface EnableServiceScan {
    
    /**
     * 指定要扫描的包路径
     * 如果未指定，将使用标注此注解的类所在的包作为基础包
     *
     * @return 要扫描的包路径数组
     */
    String[] basePackages() default {};
} 