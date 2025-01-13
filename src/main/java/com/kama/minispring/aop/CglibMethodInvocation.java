package com.kama.minispring.aop;

import net.sf.cglib.proxy.MethodProxy;
import java.lang.reflect.Method;

/**
 * CGLIB方法调用实现
 * 扩展ReflectiveMethodInvocation，支持CGLIB的方法调用
 * 
 * @author kama
 * @version 1.0.0
 */
public class CglibMethodInvocation extends ReflectiveMethodInvocation {
    
    private final MethodProxy methodProxy;

    /**
     * 构造函数
     * 
     * @param target 目标对象
     * @param method 方法
     * @param arguments 参数
     * @param methodProxy CGLIB方法代理
     */
    public CglibMethodInvocation(Object target, Method method, Object[] arguments, MethodProxy methodProxy) {
        super(target, method, arguments);
        this.methodProxy = methodProxy;
    }

    @Override
    public Object proceed() throws Throwable {
        return methodProxy.invoke(getThis(), getArguments());
    }
} 