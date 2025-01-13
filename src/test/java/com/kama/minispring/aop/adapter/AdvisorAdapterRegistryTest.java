package com.kama.minispring.aop.adapter;

import com.kama.minispring.aop.MethodBeforeAdvice;
import com.kama.minispring.aop.AfterReturningAdvice;
import com.kama.minispring.aop.MethodInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 通知适配器注册表测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class AdvisorAdapterRegistryTest {
    
    private AdvisorAdapterRegistry registry;
    
    @BeforeEach
    void setUp() {
        registry = new DefaultAdvisorAdapterRegistry();
    }
    
    @Test
    void testMethodBeforeAdvice() throws Throwable {
        TestMethodBeforeAdvice beforeAdvice = new TestMethodBeforeAdvice();
        MethodInterceptor[] interceptors = registry.getInterceptors(beforeAdvice);
        
        assertEquals(1, interceptors.length);
        assertTrue(interceptors[0] instanceof MethodBeforeAdviceInterceptor);
        
        // 测试通知执行
        Method method = String.class.getMethod("toString");
        Object target = "test";
        interceptors[0].invoke(new TestMethodInvocation(target, method));
        
        assertTrue(beforeAdvice.isExecuted());
    }
    
    @Test
    void testAfterReturningAdvice() throws Throwable {
        TestAfterReturningAdvice afterAdvice = new TestAfterReturningAdvice();
        MethodInterceptor[] interceptors = registry.getInterceptors(afterAdvice);
        
        assertEquals(1, interceptors.length);
        assertTrue(interceptors[0] instanceof AfterReturningAdviceInterceptor);
        
        // 测试通知执行
        Method method = String.class.getMethod("toString");
        Object target = "test";
        interceptors[0].invoke(new TestMethodInvocation(target, method));
        
        assertTrue(afterAdvice.isExecuted());
    }
    
    private static class TestMethodBeforeAdvice implements MethodBeforeAdvice {
        private boolean executed = false;
        
        @Override
        public void before(Method method, Object[] args, Object target) {
            executed = true;
        }
        
        public boolean isExecuted() {
            return executed;
        }
    }
    
    private static class TestAfterReturningAdvice implements AfterReturningAdvice {
        private boolean executed = false;
        
        @Override
        public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
            executed = true;
        }
        
        public boolean isExecuted() {
            return executed;
        }
    }
    
    private static class TestMethodInvocation implements com.kama.minispring.aop.MethodInvocation {
        private final Object target;
        private final Method method;
        
        public TestMethodInvocation(Object target, Method method) {
            this.target = target;
            this.method = method;
        }
        
        @Override
        public Method getMethod() {
            return method;
        }
        
        @Override
        public Object[] getArguments() {
            return new Object[0];
        }
        
        @Override
        public Object getThis() {
            return target;
        }
        
        @Override
        public Object proceed() throws Throwable {
            return method.invoke(target);
        }
    }
} 