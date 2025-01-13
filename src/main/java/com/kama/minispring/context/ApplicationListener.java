package com.kama.minispring.context;

import java.util.EventListener;

/**
 * 应用事件监听器接口
 * 所有事件监听器都应该实现此接口
 *
 * @author kama
 * @version 1.0.0
 * @param <E> 监听的事件类型
 */
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {
    
    /**
     * 处理应用事件
     *
     * @param event 要处理的事件
     */
    void onApplicationEvent(E event);
} 