package com.kama.minispring.aop;


import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * JDK动态代理测试类
 * 
 * @author kama
 * @version 1.0.0
 */
public class JdkDynamicAopProxyTest {

    interface UserService {
        String getUserName(String id);
    }

    class UserServiceImpl implements UserService {
        @Override
        public String getUserName(String id) {
            return "User:" + id;
        }
    }

    class LoggingMethodInterceptor implements MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            System.out.println("Before method: " + invocation.getMethod().getName());
            Object result = invocation.proceed();
            System.out.println("After method: " + invocation.getMethod().getName() + ", result: " + result);
            return result;
        }
    }

    class SimpleMethodMatcher implements MethodMatcher {
        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return method.getName().equals("getUserName");
        }
    }

    @Test
    public void testJdkDynamicAopProxy() throws Exception {
        // 创建目标对象
        UserService userService = new UserServiceImpl();
        
        // 创建AOP配置
        AdvisedSupport advisedSupport = new AdvisedSupport();
        advisedSupport.setTargetSource(new TargetSource(userService));
        advisedSupport.addMethodInterceptor(new LoggingMethodInterceptor());
        advisedSupport.setMethodMatcher(new SimpleMethodMatcher());
        
        // 创建代理对象
        JdkDynamicAopProxy proxy = new JdkDynamicAopProxy(advisedSupport);
        UserService proxyService = (UserService) proxy.getProxy();
        
        // 测试方法调用
        String result = proxyService.getUserName("1");
        assertEquals("User:1", result);
    }
} 