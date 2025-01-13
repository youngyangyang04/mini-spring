package com.kama.minispring.context;

import java.time.Clock;
import java.time.Instant;

/**
 * 应用事件的基类
 * 所有应用事件都应该继承此类
 *
 * @author kama
 * @version 1.0.0
 */
public abstract class ApplicationEvent {
    
    private final Instant timestamp;
    private final Object source;
    
    /**
     * 创建一个新的ApplicationEvent
     *
     * @param source 事件源对象
     */
    public ApplicationEvent(Object source) {
        this(source, Clock.systemDefaultZone());
    }
    
    /**
     * 使用指定时钟创建一个新的ApplicationEvent
     * 主要用于测试目的
     *
     * @param source 事件源对象
     * @param clock 用于获取时间戳的时钟
     */
    protected ApplicationEvent(Object source, Clock clock) {
        if (source == null) {
            throw new IllegalArgumentException("Event source cannot be null");
        }
        this.source = source;
        this.timestamp = clock.instant();
    }
    
    /**
     * 获取事件发生的时间戳
     *
     * @return 事件时间戳
     */
    public final Instant getTimestamp() {
        return this.timestamp;
    }
    
    /**
     * 获取事件源对象
     *
     * @return 事件源对象
     */
    public final Object getSource() {
        return this.source;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[source=" + source + "]";
    }
} 