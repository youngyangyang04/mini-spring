package com.kama.minispring.aop;

import java.lang.reflect.Method;

/**
 * 反射方法调用实现
 * 基于反射机制实现方法调用
 * 
 * @author kama
 * @version 1.0.0
 */
public class ReflectiveMethodInvocation implements MethodInvocation {
    
    private final Object target;
    private final Method method;
    private final Object[] arguments;

    /**
     * 构造函数
     * 
     * @param target 目标对象
     * @param method 方法
     * @param arguments 参数
     */
    public ReflectiveMethodInvocation(Object target, Method method, Object[] arguments) {
        this.target = target;
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getThis() {
        return target;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object proceed() throws Throwable {
        return method.invoke(target, arguments);
    }
} 