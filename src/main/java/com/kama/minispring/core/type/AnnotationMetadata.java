package com.kama.minispring.core.type;

import java.util.Map;
import java.util.Set;

/**
 * 用于获取类的注解元数据的接口
 * 提供访问类、方法和字段上注解信息的能力
 *
 * @author kama
 * @version 1.0.0
 */
public interface AnnotationMetadata extends ClassMetadata {
    
    /**
     * 判断类是否有指定的注解
     *
     * @param annotationName 注解的全限定名
     * @return 如果存在该注解返回true，否则返回false
     */
    boolean hasAnnotation(String annotationName);
    
    /**
     * 获取类上所有注解的名称
     *
     * @return 注解名称的集合
     */
    Set<String> getAnnotationTypes();
    
    /**
     * 获取指定注解的属性值
     *
     * @param annotationName 注解的全限定名
     * @return 注解属性名到属性值的映射
     */
    Map<String, Object> getAnnotationAttributes(String annotationName);
    
    /**
     * 判断类是否有被指定注解标注的方法
     *
     * @param annotationName 注解的全限定名
     * @return 如果存在被该注解标注的方法返回true，否则返回false
     */
    boolean hasAnnotatedMethods(String annotationName);
} 