package com.kama.minispring.context.event;

import com.kama.minispring.context.ApplicationEvent;
import com.kama.minispring.context.ApplicationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 应用事件监听器适配器
 * 将普通的方法适配为事件监听器
 *
 * @author kama
 * @version 1.0.0
 */
public class ApplicationListenerAdapter implements ApplicationListener<ApplicationEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationListenerAdapter.class);
    
    private final Object target;
    private final Method method;
    private final Class<? extends ApplicationEvent> eventType;
    
    /**
     * 创建一个新的监听器适配器
     *
     * @param target 目标对象
     * @param method 处理事件的方法
     * @param eventType 事件类型
     */
    public ApplicationListenerAdapter(Object target, Method method, Class<? extends ApplicationEvent> eventType) {
        this.target = target;
        this.method = method;
        this.eventType = eventType;
        this.method.setAccessible(true);
    }
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (eventType.isInstance(event)) {
            try {
                method.invoke(target, event);
            } catch (Exception ex) {
                logger.error("Failed to invoke event listener method: " + method, ex);
            }
        }
    }
    
    /**
     * 获取事件类型
     *
     * @return 事件类型
     */
    public Class<? extends ApplicationEvent> getEventType() {
        return this.eventType;
    }
    
    /**
     * 获取目标对象
     *
     * @return 目标对象
     */
    public Object getTarget() {
        return this.target;
    }
    
    /**
     * 获取处理方法
     *
     * @return 处理方法
     */
    public Method getMethod() {
        return this.method;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ApplicationListenerAdapter)) {
            return false;
        }
        ApplicationListenerAdapter otherAdapter = (ApplicationListenerAdapter) other;
        return (this.target.equals(otherAdapter.target) && this.method.equals(otherAdapter.method));
    }
    
    @Override
    public int hashCode() {
        return this.target.hashCode() * 31 + this.method.hashCode();
    }
    
    @Override
    public String toString() {
        return "ApplicationListenerAdapter: target = [" + this.target + "], method = [" + this.method + "]";
    }
} 