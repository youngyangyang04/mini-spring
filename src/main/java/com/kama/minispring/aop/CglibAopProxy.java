package com.kama.minispring.aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Cglib代理实现
 * 基于Cglib的代理实现
 *
 * @author kama
 * @version 1.0.0
 */
public class CglibAopProxy implements AopProxy {

    private final AdvisedSupport advised;

    /**
     * 构造函数
     *
     * @param advised AOP配置
     */
    public CglibAopProxy(AdvisedSupport advised) {
        this.advised = advised;
    }

    @Override
    public Object getProxy() {
        return getProxy(null);
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        Class<?> targetClass = advised.getTargetSource().getTargetClass();
        if (targetClass == null) {
            throw new IllegalStateException("目标类不能为空");
        }
        
        Enhancer enhancer = new Enhancer();
        if (classLoader != null) {
            enhancer.setClassLoader(classLoader);
        }
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback(new CglibMethodInterceptor());
        return enhancer.create();
    }

    /**
     * Cglib方法拦截器
     */
    private class CglibMethodInterceptor implements MethodInterceptor {
        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object target = advised.getTargetSource().getTarget();
            
            // 检查方法是否匹配切点表达式
            if (advised.getMethodMatcher() != null 
                    && !advised.getMethodMatcher().matches(method, target.getClass())) {
                return methodProxy.invoke(target, args);
            }

            // 创建拦截器链
            List<com.kama.minispring.aop.MethodInterceptor> interceptors = advised.getMethodInterceptors();

            // 创建方法调用
            CglibMethodInvocation invocation = new CglibMethodInvocation(target, method, args, methodProxy, interceptors);
            
            // 执行方法调用链
            return invocation.proceed();
        }
    }
} 