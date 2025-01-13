package com.kama.minispring.aop;

import com.kama.minispring.aop.adapter.AdvisorAdapterRegistry;
import com.kama.minispring.aop.adapter.DefaultAdvisorAdapterRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * AOP代理工厂
 * 用于创建代理对象，支持JDK动态代理和Cglib代理
 *
 * @author kama
 * @version 1.0.0
 */
public class ProxyFactory {

    private final AdvisedSupport advised;
    private final AdvisorAdapterRegistry advisorAdapterRegistry;

    /**
     * 构造函数
     *
     * @param target 目标对象
     */
    public ProxyFactory(Object target) {
        this.advised = new AdvisedSupport();
        this.advised.setTargetSource(new TargetSource(target));
        this.advisorAdapterRegistry = new DefaultAdvisorAdapterRegistry();
    }

    /**
     * 添加通知
     *
     * @param advice 通知
     */
    public void addAdvice(Advice advice) {
        MethodInterceptor interceptor = this.advisorAdapterRegistry.wrap(advice);
        this.advised.addMethodInterceptor(interceptor);
    }

    /**
     * 设置是否强制使用Cglib代理
     *
     * @param proxyTargetClass 是否强制使用Cglib代理
     */
    public void setProxyTargetClass(boolean proxyTargetClass) {
        this.advised.setProxyTargetClass(proxyTargetClass);
    }

    /**
     * 获取代理对象
     *
     * @return 代理对象
     */
    public Object getProxy() {
        return getProxy(null);
    }

    /**
     * 获取代理对象
     *
     * @param classLoader 类加载器
     * @return 代理对象
     */
    public Object getProxy(ClassLoader classLoader) {
        if (this.advised.isProxyTargetClass() || !isInterfaceProxyable(this.advised.getTargetSource().getTarget())) {
            return createCglibProxy(classLoader);
        } else {
            return createJdkDynamicProxy(classLoader);
        }
    }

    /**
     * 判断是否可以使用接口代理
     *
     * @param target 目标对象
     * @return 是否可以使用接口代理
     */
    private boolean isInterfaceProxyable(Object target) {
        Class<?> targetClass = target.getClass();
        return targetClass.getInterfaces().length > 0;
    }

    /**
     * 创建JDK动态代理
     *
     * @param classLoader 类加载器
     * @return 代理对象
     */
    private Object createJdkDynamicProxy(ClassLoader classLoader) {
        JdkDynamicAopProxy proxy = new JdkDynamicAopProxy(this.advised);
        return proxy.getProxy(classLoader != null ? classLoader : getClass().getClassLoader());
    }

    /**
     * 创建Cglib代理
     *
     * @param classLoader 类加载器
     * @return 代理对象
     */
    private Object createCglibProxy(ClassLoader classLoader) {
        CglibAopProxy proxy = new CglibAopProxy(this.advised);
        return proxy.getProxy(classLoader);
    }
} 