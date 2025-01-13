package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.BeansException;
import com.kama.minispring.beans.factory.DisposableBean;
import com.kama.minispring.beans.factory.config.BeanDefinition;

import java.lang.reflect.Method;

/**
 * 适配器模式，统一处理bean的销毁方法
 */
public class DisposableBeanAdapter implements DisposableBean {
    private final Object bean;
    private final String beanName;
    private final String destroyMethodName;
    private final boolean isDisposableBean;

    public DisposableBeanAdapter(Object bean, String beanName, BeanDefinition beanDefinition) {
        this.bean = bean;
        this.beanName = beanName;
        this.destroyMethodName = beanDefinition.getDestroyMethodName();
        this.isDisposableBean = bean instanceof DisposableBean;
    }

    @Override
    public void destroy() throws Exception {
        // 1. 如果bean实现了DisposableBean接口
        if (isDisposableBean) {
            ((DisposableBean) bean).destroy();
        }

        // 2. 如果配置了自定义的销毁方法
        if (destroyMethodName != null && !destroyMethodName.isEmpty() &&
                !(isDisposableBean && "destroy".equals(destroyMethodName))) {
            Method destroyMethod = bean.getClass().getMethod(destroyMethodName);
            destroyMethod.invoke(bean);
        }
    }
} 