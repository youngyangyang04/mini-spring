package com.kama.minispring.aop;

/**
 * 方法拦截器接口
 * 定义方法拦截的行为
 *
 * @author kama
 * @version 1.0.0
 */
public interface MethodInterceptor extends Advice {

    /**
     * 拦截方法调用
     *
     * @param invocation 方法调用
     * @return 方法返回值
     * @throws Throwable 执行异常
     */
    Object invoke(MethodInvocation invocation) throws Throwable;
} 