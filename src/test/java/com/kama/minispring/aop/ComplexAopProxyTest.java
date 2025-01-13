package com.kama.minispring.aop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 复杂AOP代理测试类
 * 测试多个通知的组合和执行顺序
 *
 * @author kama
 * @version 1.0.0
 */
public class ComplexAopProxyTest {

    private OrderService orderService;
    private List<String> executionOrder;
    private LoggingBeforeAdvice loggingBeforeAdvice;
    private ValidationBeforeAdvice validationBeforeAdvice;
    private AuditAfterAdvice auditAfterAdvice;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl();
        executionOrder = new ArrayList<>();
        loggingBeforeAdvice = new LoggingBeforeAdvice();
        validationBeforeAdvice = new ValidationBeforeAdvice();
        auditAfterAdvice = new AuditAfterAdvice();
    }

    @Test
    void testMultipleAdvices() {
        // 创建代理工厂
        ProxyFactory proxyFactory = new ProxyFactory(orderService);
        
        // 添加多个通知
        proxyFactory.addAdvice(loggingBeforeAdvice);
        proxyFactory.addAdvice(validationBeforeAdvice);
        proxyFactory.addAdvice(auditAfterAdvice);
        
        // 获取代理对象
        OrderService proxy = (OrderService) proxyFactory.getProxy();
        
        // 执行业务方法
        Order order = new Order("123", 100.0);
        proxy.createOrder(order);
        
        // 验证执行顺序
        assertEquals(4, executionOrder.size());
        assertEquals("logging", executionOrder.get(0));
        assertEquals("validation", executionOrder.get(1));
        assertEquals("createOrder", executionOrder.get(2));
        assertEquals("audit", executionOrder.get(3));
    }

    @Test
    void testAdviceWithException() {
        // 创建代理工厂
        ProxyFactory proxyFactory = new ProxyFactory(orderService);
        
        // 添加多个通知
        proxyFactory.addAdvice(loggingBeforeAdvice);
        proxyFactory.addAdvice(validationBeforeAdvice);
        proxyFactory.addAdvice(auditAfterAdvice);
        
        // 获取代理对象
        OrderService proxy = (OrderService) proxyFactory.getProxy();
        
        // 执行业务方法，使用无效订单金额
        Order invalidOrder = new Order("123", -100.0);
        
        // 验证异常抛出
        assertThrows(IllegalArgumentException.class, () -> {
            proxy.createOrder(invalidOrder);
        });
        
        // 验证执行顺序（只有前置通知被执行）
        assertEquals(2, executionOrder.size());
        assertEquals("logging", executionOrder.get(0));
        assertEquals("validation", executionOrder.get(1));
    }

    // 测试接口和类
    interface OrderService {
        void createOrder(Order order);
    }

    class Order {
        private String orderId;
        private double amount;

        public Order(String orderId, double amount) {
            this.orderId = orderId;
            this.amount = amount;
        }

        public String getOrderId() {
            return orderId;
        }

        public double getAmount() {
            return amount;
        }
    }

    class OrderServiceImpl implements OrderService {
        @Override
        public void createOrder(Order order) {
            executionOrder.add("createOrder");
        }
    }

    // 测试通知类
    class LoggingBeforeAdvice implements MethodBeforeAdvice {
        @Override
        public void before(Method method, Object[] args, Object target) {
            executionOrder.add("logging");
        }
    }

    class ValidationBeforeAdvice implements MethodBeforeAdvice {
        @Override
        public void before(Method method, Object[] args, Object target) {
            executionOrder.add("validation");
            Order order = (Order) args[0];
            if (order.getAmount() <= 0) {
                throw new IllegalArgumentException("Order amount must be positive");
            }
        }
    }

    class AuditAfterAdvice implements AfterReturningAdvice {
        @Override
        public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
            executionOrder.add("audit");
            Order order = (Order) args[0];
            // 模拟审计日志记录
            System.out.println("Audit: Order " + order.getOrderId() + " created successfully");
        }
    }
} 