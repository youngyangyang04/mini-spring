package com.kama.minispring.core.env;

/**
 * 环境配置接口，用于获取配置属性和环境信息
 * 
 * @author kama
 * @version 1.0.0
 */
public interface Environment {
    
    /**
     * 获取属性值
     *
     * @param key 属性键
     * @return 属性值，如果不存在返回null
     */
    String getProperty(String key);
    
    /**
     * 获取属性值，如果不存在返回默认值
     *
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 属性值，如果不存在返回默认值
     */
    String getProperty(String key, String defaultValue);
    
    /**
     * 判断是否包含指定属性
     *
     * @param key 属性键
     * @return 如果包含返回true，否则返回false
     */
    boolean containsProperty(String key);
    
    /**
     * 获取激活的配置文件
     *
     * @return 激活的配置文件数组
     */
    String[] getActiveProfiles();
    
    /**
     * 获取默认的配置文件
     *
     * @return 默认的配置文件数组
     */
    String[] getDefaultProfiles();
    
    /**
     * 判断指定的配置文件是否激活
     *
     * @param profile 配置文件名称
     * @return 如果激活返回true，否则返回false
     */
    boolean acceptsProfiles(String... profile);
} 