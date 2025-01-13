package com.kama.minispring.aop;

import net.sf.cglib.proxy.MethodProxy;
import com.kama.minispring.aop.adapter.MethodBeforeAdviceInterceptor;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Cglib方法调用实现
 * 基于Cglib的方法调用实现
 *
 * @author kama
 * @version 1.0.0
 */
public class CglibMethodInvocation extends ReflectiveMethodInvocation {
    
    private final MethodProxy methodProxy;
    private int currentInterceptorIndex = -1;

    /**
     * 构造函数
     *
     * @param target 目标对象
     * @param method 方法
     * @param args 参数
     * @param methodProxy 方法代理
     * @param interceptors 拦截器列表
     */
    public CglibMethodInvocation(Object target, Method method, Object[] args, 
            MethodProxy methodProxy, List<MethodInterceptor> interceptors) {
        super(target, method, args, interceptors);
        this.methodProxy = methodProxy;
    }

    @Override
    public Object proceed() throws Throwable {
        if (currentInterceptorIndex >= interceptors.size() - 1) {
            return methodProxy.invoke(getThis(), getArguments());
        }

        // 获取下一个拦截器
        MethodInterceptor interceptor = interceptors.get(++currentInterceptorIndex);
        
        try {
            // 调用拦截器
            return interceptor.invoke(this);
        } catch (Throwable ex) {
            // 如果发生异常，确保所有前置通知都已执行
            if (interceptor instanceof MethodBeforeAdviceInterceptor) {
                currentInterceptorIndex++;
                if (currentInterceptorIndex < interceptors.size() 
                        && interceptors.get(currentInterceptorIndex) instanceof MethodBeforeAdviceInterceptor) {
                    return proceed();
                }
            }
            throw ex;
        }
    }
} 