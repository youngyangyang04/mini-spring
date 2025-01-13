package com.kama.minispring.core.io;

/**
 * 资源加载器接口
 * 定义了加载资源的规范
 *
 * @author kama
 * @version 1.0.0
 */
public interface ResourceLoader {
    
    /** 类路径URL前缀 */
    String CLASSPATH_URL_PREFIX = "classpath:";
    
    /**
     * 获取资源
     *
     * @param location 资源位置
     * @return 资源对象
     */
    Resource getResource(String location);
    
    /**
     * 获取类加载器
     *
     * @return 类加载器
     */
    ClassLoader getClassLoader();
} 