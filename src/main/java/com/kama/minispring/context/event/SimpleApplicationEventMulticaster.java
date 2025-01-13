package com.kama.minispring.context.event;

import com.kama.minispring.context.ApplicationEvent;
import com.kama.minispring.context.ApplicationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * ApplicationEventMulticaster的简单实现
 * 提供了基本的事件多播功能
 *
 * @author kama
 * @version 1.0.0
 */
public class SimpleApplicationEventMulticaster implements ApplicationEventMulticaster {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleApplicationEventMulticaster.class);
    
    private final Set<ApplicationListener<?>> listeners = new LinkedHashSet<>();
    private Executor taskExecutor;
    
    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        synchronized (this.listeners) {
            this.listeners.add(listener);
            logger.debug("Added application listener: {}", listener);
        }
    }
    
    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
        synchronized (this.listeners) {
            this.listeners.remove(listener);
            logger.debug("Removed application listener: {}", listener);
        }
    }
    
    @Override
    public void removeAllListeners() {
        synchronized (this.listeners) {
            this.listeners.clear();
            logger.debug("Removed all application listeners");
        }
    }
    
    @Override
    public void multicastEvent(final ApplicationEvent event) {
        for (final ApplicationListener<?> listener : getApplicationListeners(event)) {
            Executor executor = getTaskExecutor();
            if (executor != null) {
                executor.execute(() -> invokeListener(listener, event));
            } else {
                invokeListener(listener, event);
            }
        }
    }
    
    /**
     * 获取任务执行器
     *
     * @return 任务执行器
     */
    protected Executor getTaskExecutor() {
        return this.taskExecutor;
    }
    
    /**
     * 设置任务执行器
     *
     * @param taskExecutor 任务执行器
     */
    public void setTaskExecutor(Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }
    
    /**
     * 获取适用于指定事件的所有监听器
     */
    protected Collection<ApplicationListener<?>> getApplicationListeners(ApplicationEvent event) {
        Set<ApplicationListener<?>> allListeners = new LinkedHashSet<>();
        synchronized (this.listeners) {
            for (ApplicationListener<?> listener : this.listeners) {
                if (supportsEvent(listener, event)) {
                    allListeners.add(listener);
                }
            }
        }
        return allListeners;
    }
    
    /**
     * 检查监听器是否支持指定的事件
     */
    protected boolean supportsEvent(ApplicationListener<?> listener, ApplicationEvent event) {
        Class<?> listenerClass = listener.getClass();
        
        // 首先检查类本身实现的接口
        if (supportsEventForInterfaces(listenerClass.getGenericInterfaces(), event)) {
            return true;
        }
        
        // 然后检查父类实现的接口
        Class<?> superclass = listenerClass.getSuperclass();
        while (superclass != null && superclass != Object.class) {
            if (supportsEventForInterfaces(superclass.getGenericInterfaces(), event)) {
                return true;
            }
            superclass = superclass.getSuperclass();
        }
        
        return false;
    }
    
    /**
     * 检查给定的接口类型是否支持指定的事件
     */
    private boolean supportsEventForInterfaces(Type[] genericInterfaces, ApplicationEvent event) {
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                Type rawType = parameterizedType.getRawType();
                
                if (rawType == ApplicationListener.class) {
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length == 1) {
                        Type typeArgument = typeArguments[0];
                        if (typeArgument instanceof Class<?>) {
                            Class<?> eventClass = (Class<?>) typeArgument;
                            return eventClass.isInstance(event);
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 调用监听器处理事件
     */
    @SuppressWarnings("unchecked")
    protected void invokeListener(ApplicationListener listener, ApplicationEvent event) {
        try {
            listener.onApplicationEvent(event);
        } catch (Exception ex) {
            logger.error("Error invoking ApplicationListener", ex);
        }
    }
} 