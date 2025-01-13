package com.kama.minispring.aop;

import java.lang.reflect.Method;

/**
 * 方法调用接口
 * 封装方法调用的相关信息
 * 
 * @author kama
 * @version 1.0.0
 */
public interface MethodInvocation {
    
    /**
     * 获取方法
     */
    Method getMethod();
    
    /**
     * 获取目标对象
     */
    Object getThis();
    
    /**
     * 获取方法参数
     */
    Object[] getArguments();
    
    /**
     * 执行方法调用
     * 
     * @return 方法执行结果
     * @throws Throwable 执行异常
     */
    Object proceed() throws Throwable;
} 