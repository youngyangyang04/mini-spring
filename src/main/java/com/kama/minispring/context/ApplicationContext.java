package com.kama.minispring.context;

import com.kama.minispring.beans.factory.ListableBeanFactory;
import com.kama.minispring.core.io.ResourceLoader;

/**
 * 应用上下文的中央接口
 * 扩展了ListableBeanFactory，提供了更多的应用层特性
 * 
 * @author kama
 * @version 1.0.0
 */
public interface ApplicationContext extends ListableBeanFactory, ResourceLoader {
    
    /**
     * 获取应用上下文的唯一ID
     *
     * @return 应用上下文ID
     */
    String getId();
    
    /**
     * 获取应用上下文的显示名称
     *
     * @return 显示名称
     */
    String getDisplayName();
    
    /**
     * 获取应用上下文的启动时间
     *
     * @return 启动时间戳
     */
    long getStartupDate();
    
    /**
     * 获取父级上下文
     *
     * @return 父级上下文，如果没有则返回null
     */
    ApplicationContext getParent();
} 