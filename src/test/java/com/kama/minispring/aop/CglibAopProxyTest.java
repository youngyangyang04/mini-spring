package com.kama.minispring.aop;


import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CGLIB代理测试类
 * 
 * @author kama
 * @version 1.0.0
 */
public class CglibAopProxyTest {

    /**
     * 测试类
     */
    static class TestService {
        public String sayHello(String name) {
            return "Hello, " + name;
        }
    }

    /**
     * 简单方法匹配器
     */
    static class SimpleMethodMatcher implements MethodMatcher {
        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return method.getName().equals("sayHello");
        }
    }

    /**
     * 日志拦截器
     */
    static class LoggingMethodInterceptor implements com.kama.minispring.aop.MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            System.out.println("Before method: " + invocation.getMethod().getName());
            Object result = invocation.proceed();
            System.out.println("After method: " + invocation.getMethod().getName() + ", result: " + result);
            return result;
        }
    }

    @Test
    public void testCglibProxy() throws Exception {
        // 创建目标对象
        TestService target = new TestService();
        
        // 创建AOP配置
        AdvisedSupport advisedSupport = new AdvisedSupport();
        advisedSupport.setTargetSource(new TargetSource(target));
        advisedSupport.addMethodInterceptor(new LoggingMethodInterceptor());
        advisedSupport.setMethodMatcher(new SimpleMethodMatcher());
        
        // 创建代理对象
        CglibAopProxy proxy = new CglibAopProxy(advisedSupport);
        TestService proxyObject = (TestService) proxy.getProxy();
        
        // 调用代理方法
        String result = proxyObject.sayHello("Kama");
        assertEquals("Hello, Kama", result);
    }
} 