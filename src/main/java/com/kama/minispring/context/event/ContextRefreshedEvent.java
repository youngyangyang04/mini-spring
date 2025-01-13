package com.kama.minispring.context.event;

import com.kama.minispring.context.ApplicationContext;

/**
 * 应用上下文刷新事件
 * 在应用上下文完成刷新时发布
 *
 * @author kama
 * @version 1.0.0
 */
public class ContextRefreshedEvent extends ContextEvent {
    
    /**
     * 创建一个新的上下文刷新事件
     *
     * @param source 事件源（应用上下文）
     */
    public ContextRefreshedEvent(ApplicationContext source) {
        super(source);
    }
} 