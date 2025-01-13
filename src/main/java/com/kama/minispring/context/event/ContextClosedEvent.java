package com.kama.minispring.context.event;

import com.kama.minispring.context.ApplicationContext;

/**
 * 应用上下文关闭事件
 * 在应用上下文关闭时发布
 *
 * @author kama
 * @version 1.0.0
 */
public class ContextClosedEvent extends ContextEvent {
    
    /**
     * 创建一个新的上下文关闭事件
     *
     * @param source 事件源（应用上下文）
     */
    public ContextClosedEvent(ApplicationContext source) {
        super(source);
    }
} 