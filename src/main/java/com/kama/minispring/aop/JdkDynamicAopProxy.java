package com.kama.minispring.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * JDK动态代理实现
 * 基于JDK动态代理实现AOP代理
 * 
 * @author kama
 * @version 1.0.0
 */
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    private final AdvisedSupport advised;

    /**
     * 构造函数
     * 
     * @param advised AOP配置
     */
    public JdkDynamicAopProxy(AdvisedSupport advised) {
        this.advised = advised;
    }

    @Override
    public Object getProxy() {
        return getProxy(getClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        Class<?> targetClass = advised.getTargetSource().getTargetClass();
        if (targetClass == null) {
            throw new IllegalStateException("目标类不能为空");
        }
        return Proxy.newProxyInstance(classLoader, targetClass.getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object target = advised.getTargetSource().getTarget();
        
        // 检查方法是否匹配切点表达式
        if (advised.getMethodMatcher() != null 
                && !advised.getMethodMatcher().matches(method, target.getClass())) {
            return method.invoke(target, args);
        }

        // 创建拦截器链
        List<MethodInterceptor> interceptors = advised.getMethodInterceptors();

        // 创建方法调用对象
        MethodInvocation invocation = new ReflectiveMethodInvocation(target, method, args, interceptors);
        
        // 执行拦截器链
        return invocation.proceed();
    }
} 