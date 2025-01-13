package com.kama.minispring.aop;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AOP代理测试类
 * 测试AOP在实际业务场景中的应用
 *
 * @author kama
 * @version 1.0.0
 */
public class AopProxyTest {

    private UserService userService;
    private TestMethodBeforeAdvice beforeAdvice;
    private TestAfterReturningAdvice afterAdvice;
    private List<String> executionOrder;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
        beforeAdvice = new TestMethodBeforeAdvice();
        afterAdvice = new TestAfterReturningAdvice();
        executionOrder = new ArrayList<>();
    }

    @Test
    void testAopProxyWithAdvices() {
        // 创建代理工厂
        ProxyFactory proxyFactory = new ProxyFactory(userService);
        
        // 添加通知
        proxyFactory.addAdvice(beforeAdvice);
        proxyFactory.addAdvice(afterAdvice);
        
        // 获取代理对象
        UserService proxy = (UserService) proxyFactory.getProxy();
        
        // 执行业务方法
        String result = proxy.findUser("test");
        
        // 验证结果
        assertEquals("User: test", result);
        assertEquals(3, executionOrder.size());
        assertEquals("before", executionOrder.get(0));
        assertEquals("findUser", executionOrder.get(1));
        assertEquals("afterReturning", executionOrder.get(2));
    }

    // 测试接口
    interface UserService {
        String findUser(String username);
    }

    // 测试实现类
    class UserServiceImpl implements UserService {
        @Override
        public String findUser(String username) {
            executionOrder.add("findUser");
            return "User: " + username;
        }
    }

    // 测试前置通知
    class TestMethodBeforeAdvice implements MethodBeforeAdvice {
        @Override
        public void before(Method method, Object[] args, Object target) {
            executionOrder.add("before");
        }
    }

    // 测试后置通知
    class TestAfterReturningAdvice implements AfterReturningAdvice {
        @Override
        public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
            executionOrder.add("afterReturning");
        }
    }
} 