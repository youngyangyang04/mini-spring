package com.kama.minispring.cloud.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务元数据，用于存储服务的附加信息
 * 
 * @author kama
 * @version 1.0.0
 */
public class ServiceMetadata {
    private final Map<String, String> metadata;
    
    public ServiceMetadata() {
        this.metadata = new HashMap<>();
    }
    
    public ServiceMetadata(Map<String, String> metadata) {
        this.metadata = new HashMap<>(metadata);
    }
    
    /**
     * 添加元数据
     *
     * @param key 键
     * @param value 值
     */
    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }
    
    /**
     * 获取元数据值
     *
     * @param key 键
     * @return 值
     */
    public String getMetadata(String key) {
        return metadata.get(key);
    }
    
    /**
     * 获取所有元数据
     *
     * @return 元数据Map的不可修改视图
     */
    public Map<String, String> getAllMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
} 