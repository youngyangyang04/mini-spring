package com.kama.minispring.aop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.kama.minispring.aop.adapter.DefaultAdvisorAdapterRegistry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cglib代理测试类
 * 测试Cglib代理的功能
 *
 * @author kama
 * @version 1.0.0
 */
public class CglibProxyTest {

    private UserService userService;
    private List<String> executionOrder;
    private LoggingBeforeAdvice loggingAdvice;
    private AuditAfterAdvice auditAdvice;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        executionOrder = new ArrayList<>();
        userService.setExecutionOrder(executionOrder);
        loggingAdvice = new LoggingBeforeAdvice();
        auditAdvice = new AuditAfterAdvice();
    }

    @Test
    void testCglibProxy() {
        // 创建代理工厂
        ProxyFactory proxyFactory = new ProxyFactory(userService);
        proxyFactory.setProxyTargetClass(true); // 强制使用Cglib代理
        
        // 添加通知
        proxyFactory.addAdvice(loggingAdvice);
        proxyFactory.addAdvice(auditAdvice);
        
        // 获取代理对象
        UserService proxy = (UserService) proxyFactory.getProxy();
        
        // 执行业务方法
        String result = proxy.findUser("test");
        
        // 验证结果
        assertEquals("User: test", result);
        assertEquals(3, executionOrder.size());
        assertEquals("logging", executionOrder.get(0));
        assertEquals("findUser", executionOrder.get(1));
        assertEquals("audit", executionOrder.get(2));
    }

    // 测试类（不实现任何接口）
    static class UserService {
        private List<String> executionOrder;
        
        public UserService() {
            // 无参构造函数
        }
        
        public void setExecutionOrder(List<String> executionOrder) {
            this.executionOrder = executionOrder;
        }
        
        public String findUser(String username) {
            executionOrder.add("findUser");
            return "User: " + username;
        }
    }

    // 测试通知类
    class LoggingBeforeAdvice implements MethodBeforeAdvice {
        @Override
        public void before(Method method, Object[] args, Object target) {
            executionOrder.add("logging");
        }
    }

    class AuditAfterAdvice implements AfterReturningAdvice {
        @Override
        public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
            executionOrder.add("audit");
        }
    }
} 