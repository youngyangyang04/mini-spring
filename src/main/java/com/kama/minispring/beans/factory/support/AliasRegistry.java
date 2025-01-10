package com.kama.minispring.beans.factory.support;

/**
 * 别名注册表接口，提供别名的注册和解析功能
 * 
 * @author kama
 * @version 1.0.0
 */
public interface AliasRegistry {
    
    /**
     * 注册一个别名
     *
     * @param name bean的名称
     * @param alias 要注册的别名
     */
    void registerAlias(String name, String alias);
    
    /**
     * 移除一个别名
     *
     * @param alias 要移除的别名
     */
    void removeAlias(String alias);
    
    /**
     * 判断指定的名称是否是别名
     *
     * @param name 要检查的名称
     * @return 如果是别名返回true，否则返回false
     */
    boolean isAlias(String name);
    
    /**
     * 获取指定名称的所有别名
     *
     * @param name bean的名称
     * @return 别名数组
     */
    String[] getAliases(String name);
} 