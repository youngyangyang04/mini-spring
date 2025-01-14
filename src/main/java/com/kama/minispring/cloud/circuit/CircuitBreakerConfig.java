package com.kama.minispring.cloud.circuit;

import java.time.Duration;

/**
 * 熔断器配置类
 * 
 * @author kama
 * @version 1.0.0
 */
public class CircuitBreakerConfig {
    
    private final int failureThreshold;
    private final Duration waitDuration;
    private final int permittedNumberOfCallsInHalfOpenState;
    
    private CircuitBreakerConfig(Builder builder) {
        this.failureThreshold = builder.failureThreshold;
        this.waitDuration = builder.waitDuration;
        this.permittedNumberOfCallsInHalfOpenState = builder.permittedNumberOfCallsInHalfOpenState;
    }
    
    public int getFailureThreshold() {
        return failureThreshold;
    }
    
    public Duration getWaitDuration() {
        return waitDuration;
    }
    
    public int getPermittedNumberOfCallsInHalfOpenState() {
        return permittedNumberOfCallsInHalfOpenState;
    }
    
    /**
     * 配置构建器
     */
    public static class Builder {
        private int failureThreshold = 5;
        private Duration waitDuration = Duration.ofSeconds(60);
        private int permittedNumberOfCallsInHalfOpenState = 10;
        
        /**
         * 设置失败阈值
         *
         * @param failureThreshold 连续失败次数阈值
         * @return 构建器实例
         */
        public Builder failureThreshold(int failureThreshold) {
            if (failureThreshold <= 0) {
                throw new IllegalArgumentException("Failure threshold must be greater than 0");
            }
            this.failureThreshold = failureThreshold;
            return this;
        }
        
        /**
         * 设置等待时间
         *
         * @param waitDuration 从开启状态转换到半开状态的等待时间
         * @return 构建器实例
         */
        public Builder waitDuration(Duration waitDuration) {
            if (waitDuration == null || waitDuration.isNegative() || waitDuration.isZero()) {
                throw new IllegalArgumentException("Wait duration must be positive");
            }
            this.waitDuration = waitDuration;
            return this;
        }
        
        /**
         * 设置半开状态允许的调用次数
         *
         * @param permittedNumberOfCallsInHalfOpenState 半开状态下允许的调用次数
         * @return 构建器实例
         */
        public Builder permittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
            if (permittedNumberOfCallsInHalfOpenState <= 0) {
                throw new IllegalArgumentException("Permitted number of calls must be greater than 0");
            }
            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
            return this;
        }
        
        /**
         * 构建配置实例
         *
         * @return 熔断器配置实例
         */
        public CircuitBreakerConfig build() {
            return new CircuitBreakerConfig(this);
        }
    }
} 