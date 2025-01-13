package com.kama.minispring.aop;

import java.lang.reflect.Method;

/**
 * 方法匹配器
 * 用于判断目标方法是否符合切点表达式
 * 
 * @author kama
 * @version 1.0.0
 */
public interface MethodMatcher {
    
    /**
     * 判断目标方法是否符合切点表达式
     * 
     * @param method 目标方法
     * @param targetClass 目标类
     * @return 是否匹配
     */
    boolean matches(Method method, Class<?> targetClass);
} 