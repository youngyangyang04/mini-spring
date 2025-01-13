package com.kama.minispring.context.event;

import com.kama.minispring.context.ApplicationContext;
import com.kama.minispring.context.ApplicationEvent;

/**
 * 应用上下文事件的抽象基类
 * 所有与应用上下文相关的事件都应该继承此类
 *
 * @author kama
 * @version 1.0.0
 */
public abstract class ContextEvent extends ApplicationEvent {
    
    /**
     * 创建一个新的上下文事件
     *
     * @param source 事件源（应用上下文）
     */
    public ContextEvent(ApplicationContext source) {
        super(source);
    }
    
    /**
     * 获取产生事件的应用上下文
     *
     * @return 应用上下文
     */
    public final ApplicationContext getApplicationContext() {
        return (ApplicationContext) getSource();
    }
} 