package com.kama.minispring.aop;

/**
 * AOP配置管理类
 * 存储AOP代理的配置信息,包括目标对象、拦截器等
 * 
 * @author kama
 * @version 1.0.0
 */
public class AdvisedSupport {
    
    // 是否使用CGLIB代理
    private boolean proxyTargetClass = false;
    
    // 目标对象
    private TargetSource targetSource;
    
    // 方法拦截器
    private MethodInterceptor methodInterceptor;
    
    // 方法匹配器(检查目标方法是否符合通知条件)
    private MethodMatcher methodMatcher;

    public boolean isProxyTargetClass() {
        return proxyTargetClass;
    }

    public void setProxyTargetClass(boolean proxyTargetClass) {
        this.proxyTargetClass = proxyTargetClass;
    }

    public TargetSource getTargetSource() {
        return targetSource;
    }

    public void setTargetSource(TargetSource targetSource) {
        this.targetSource = targetSource;
    }

    public MethodInterceptor getMethodInterceptor() {
        return methodInterceptor;
    }

    public void setMethodInterceptor(MethodInterceptor methodInterceptor) {
        this.methodInterceptor = methodInterceptor;
    }

    public MethodMatcher getMethodMatcher() {
        return methodMatcher;
    }

    public void setMethodMatcher(MethodMatcher methodMatcher) {
        this.methodMatcher = methodMatcher;
    }
} 