package com.kama.minispring.cloud.registry;

/**
 * 服务状态枚举，定义服务实例的可能状态
 * 
 * @author kama
 * @version 1.0.0
 */
public enum ServiceStatus {
    /**
     * 服务正常运行
     */
    UP,
    
    /**
     * 服务已下线
     */
    DOWN,
    
    /**
     * 服务不可用
     */
    OUT_OF_SERVICE,
    
    /**
     * 服务未知状态
     */
    UNKNOWN
} 