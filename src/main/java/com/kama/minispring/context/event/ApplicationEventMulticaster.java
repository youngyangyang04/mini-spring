package com.kama.minispring.context.event;

import com.kama.minispring.context.ApplicationEvent;
import com.kama.minispring.context.ApplicationListener;

/**
 * 应用事件多播器接口
 * 提供了注册监听器和多播事件的功能
 *
 * @author kama
 * @version 1.0.0
 */
public interface ApplicationEventMulticaster {
    
    /**
     * 添加一个监听器
     *
     * @param listener 要添加的监听器
     */
    void addApplicationListener(ApplicationListener<?> listener);
    
    /**
     * 移除一个监听器
     *
     * @param listener 要移除的监听器
     */
    void removeApplicationListener(ApplicationListener<?> listener);
    
    /**
     * 移除所有监听器
     */
    void removeAllListeners();
    
    /**
     * 将事件多播给所有适当的监听器
     *
     * @param event 要多播的事件
     */
    void multicastEvent(ApplicationEvent event);
} 