package com.kama.minispring.cloud.circuit;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 默认的熔断器实现
 * 
 * @author kama
 * @version 1.0.0
 */
public class DefaultCircuitBreaker<T> implements CircuitBreaker<T> {
    
    private final String name;
    private final CircuitBreakerConfig config;
    private final AtomicReference<State> state;
    private final AtomicInteger failureCount;
    private final AtomicInteger successCount;
    private final AtomicReference<Instant> lastFailureTime;
    private final AtomicReference<Instant> lastStateTransitionTime;
    private final Object halfOpenLock;
    
    public DefaultCircuitBreaker(String name, CircuitBreakerConfig config) {
        this.name = name;
        this.config = config;
        this.state = new AtomicReference<>(State.CLOSED);
        this.failureCount = new AtomicInteger(0);
        this.successCount = new AtomicInteger(0);
        this.lastStateTransitionTime = new AtomicReference<>(Instant.now());
        this.lastFailureTime = new AtomicReference<>(Instant.now());
        this.halfOpenLock = new Object();
    }
    
    @Override
    public T execute(Supplier<T> operation, Supplier<T> fallback) {
        while (true) {
            State currentState = state.get();
            
            switch (currentState) {
                case CLOSED:
                    if (successCount.get() >= config.getPermittedNumberOfCallsInHalfOpenState()) {
                        return fallback.get();
                    }
                    try {
                        T result = operation.get();
                        onSuccess();
                        return result;
                    } catch (Exception e) {
                        onFailure();
                        return fallback.get();
                    }
                    
                case OPEN:
                    if (shouldTransitionToHalfOpen()) {
                        if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                            lastStateTransitionTime.set(Instant.now());
                            successCount.set(0);
                            continue;
                        }
                    }
                    return fallback.get();
                    
                case HALF_OPEN:
                    synchronized (halfOpenLock) {
                        if (state.get() != State.HALF_OPEN) {
                            continue;
                        }
                        
                        int currentSuccessCount = successCount.get();
                        if (currentSuccessCount >= config.getPermittedNumberOfCallsInHalfOpenState()) {
                            if (state.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
                                lastStateTransitionTime.set(Instant.now());
                                failureCount.set(0);
                            }
                            return fallback.get();
                        }
                        
                        try {
                            T result = operation.get();
                            currentSuccessCount = successCount.incrementAndGet();
                            
                            if (currentSuccessCount >= config.getPermittedNumberOfCallsInHalfOpenState()) {
                                if (state.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
                                    lastStateTransitionTime.set(Instant.now());
                                    failureCount.set(0);
                                }
                            }
                            return result;
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            
                            if (state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
                                lastStateTransitionTime.set(Instant.now());
                                successCount.set(0);
                                failureCount.set(0);
                            }
                            return fallback.get();
                        }
                    }
                    
                default:
                    throw new IllegalStateException("Unknown circuit breaker state: " + currentState);
            }
        }
    }
    
    private boolean shouldTransitionToHalfOpen() {
        Duration elapsedTime = Duration.between(lastStateTransitionTime.get(), Instant.now());
        return elapsedTime.compareTo(config.getWaitDuration()) >= 0;
    }
    
    private void onSuccess() {
        failureCount.set(0);
    }
    
    private void onFailure() {
        int failures = failureCount.incrementAndGet();
        lastFailureTime.set(Instant.now());
        
        if (failures >= config.getFailureThreshold()) {
            if (state.compareAndSet(State.CLOSED, State.OPEN)) {
                lastStateTransitionTime.set(Instant.now());
                successCount.set(0);
                failureCount.set(0);
            }
        }
    }
    
    @Override
    public State getState() {
        State currentState = state.get();
        if (currentState == State.OPEN && shouldTransitionToHalfOpen()) {
            if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                lastStateTransitionTime.set(Instant.now());
                successCount.set(0);
                failureCount.set(0);
            }
            return state.get();
        }
        return currentState;
    }
    
    @Override
    public void reset() {
        state.set(State.CLOSED);
        successCount.set(0);
        failureCount.set(0);
        lastStateTransitionTime.set(Instant.now());
    }
    
    @Override
    public String getName() {
        return name;
    }
} 