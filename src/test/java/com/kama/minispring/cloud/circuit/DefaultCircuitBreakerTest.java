package com.kama.minispring.cloud.circuit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 默认熔断器测试类
 * 
 * @author kama
 * @version 1.0.0
 */
class DefaultCircuitBreakerTest {
    
    private CircuitBreaker<String> circuitBreaker;
    private CircuitBreakerConfig config;
    
    @BeforeEach
    void setUp() {
        config = new CircuitBreakerConfig.Builder()
                .failureThreshold(3)
                .waitDuration(Duration.ofMillis(100))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();
        circuitBreaker = new DefaultCircuitBreaker<String>("test", config);
    }
    
    @Test
    void shouldStartInClosedState() {
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }
    
    @Test
    void shouldTransitionToOpenStateAfterFailures() {
        // 执行失败操作直到达到阈值
        for (int i = 0; i < config.getFailureThreshold(); i++) {
            circuitBreaker.execute(
                () -> { throw new RuntimeException("Simulated failure"); },
                () -> "fallback"
            );
        }
        
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
    }
    
    @Test
    void shouldTransitionToHalfOpenStateAfterWaitDuration() throws InterruptedException {
        // 先转换到OPEN状态
        for (int i = 0; i < config.getFailureThreshold(); i++) {
            circuitBreaker.execute(
                () -> { throw new RuntimeException("Simulated failure"); },
                () -> "fallback"
            );
        }
        
        // 等待转换时间
        Thread.sleep(config.getWaitDuration().toMillis() + 10);
        
        // 验证状态
        assertEquals(CircuitBreaker.State.HALF_OPEN, circuitBreaker.getState());
    }
    
    @Test
    void shouldTransitionToClosedStateAfterSuccessInHalfOpen() throws InterruptedException {
        // 先转换到OPEN状态
        for (int i = 0; i < config.getFailureThreshold(); i++) {
            circuitBreaker.execute(
                () -> { throw new RuntimeException("Simulated failure"); },
                () -> "fallback"
            );
        }
        
        // 等待转换时间
        Thread.sleep(config.getWaitDuration().toMillis() + 10);
        
        // 执行成功操作，应该最终转换到CLOSED状态
        for (int i = 0; i < config.getPermittedNumberOfCallsInHalfOpenState(); i++) {
            String result = circuitBreaker.execute(() -> "success", () -> "fallback");
            assertEquals("success", result);
        }
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }
    
    @Test
    void shouldUseFallbackWhenOpen() {
        // 先转换到OPEN状态
        for (int i = 0; i < config.getFailureThreshold(); i++) {
            circuitBreaker.execute(
                () -> { throw new RuntimeException("Simulated failure"); },
                () -> "fallback"
            );
        }
        
        // 在OPEN状态下执行操作应该直接返回fallback结果
        String result = circuitBreaker.execute(() -> "success", () -> "fallback");
        assertEquals("fallback", result);
    }
    
    @Test
    void shouldLimitCallsInHalfOpenState() throws InterruptedException {
        // 设置熔断器配置
        CircuitBreakerConfig config = new CircuitBreakerConfig.Builder()
                .failureThreshold(2)
                .waitDuration(Duration.ofMillis(100))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();
                
        // 创建熔断器实例
        DefaultCircuitBreaker<String> circuitBreaker = new DefaultCircuitBreaker<>("test", config);
        
        // 触发熔断器进入开启状态
        for (int i = 0; i < config.getFailureThreshold(); i++) {
            circuitBreaker.execute(() -> { throw new RuntimeException("Simulated failure"); }, () -> "fallback");
        }
        
        // 等待转换到半开状态
        Thread.sleep(config.getWaitDuration().toMillis() + 10);
        
        // 验证在半开状态下的调用限制
        int totalCalls = 5;
        int successCount = 0;
        int fallbackCount = 0;
        
        for (int i = 0; i < totalCalls; i++) {
            String result = circuitBreaker.execute(() -> "success", () -> "fallback");
            if ("success".equals(result)) {
                successCount++;
            } else if ("fallback".equals(result)) {
                fallbackCount++;
            }
        }
        
        // 验证成功调用次数不超过允许的次数
        assertTrue(successCount <= config.getPermittedNumberOfCallsInHalfOpenState(),
                "Success calls should not exceed permitted number");
                
        // 验证总调用次数等于成功调用和fallback调用之和
        assertEquals(totalCalls, successCount + fallbackCount,
                "Total calls should equal sum of success and fallback calls");
    }
    
    @Test
    void shouldResetToClosedState() {
        // 先转换到OPEN状态
        for (int i = 0; i < config.getFailureThreshold(); i++) {
            circuitBreaker.execute(
                () -> { throw new RuntimeException("Simulated failure"); },
                () -> "fallback"
            );
        }
        
        // 重置状态
        circuitBreaker.reset();
        
        // 验证状态
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        
        // 验证可以正常执行
        String result = circuitBreaker.execute(() -> "success", () -> "fallback");
        assertEquals("success", result);
    }
} 