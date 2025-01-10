package com.kama.minispring.beans.factory.support;

import com.kama.minispring.beans.BeansException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AliasRegistry接口的简单实现，提供基本的别名注册和解析功能
 * 
 * @author kama
 * @version 1.0.0
 */
public class SimpleAliasRegistry implements AliasRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleAliasRegistry.class);
    
    /** 别名映射：key是别名，value是bean名称 */
    private final Map<String, String> aliasMap = new ConcurrentHashMap<>(16);
    
    @Override
    public void registerAlias(String name, String alias) {
        if (alias.equals(name)) {
            removeAlias(alias);
            return;
        }
        
        // 检查循环引用
        if (hasAliasCycle(name, alias)) {
            throw new BeansException("Circular reference between alias '" + alias + "' and name '" + name + "'");
        }
        
        String registeredName = aliasMap.get(alias);
        if (registeredName != null) {
            if (registeredName.equals(name)) {
                // 已经注册过相同的别名，直接返回
                return;
            }
            throw new BeansException("Cannot register alias '" + alias + "' for name '" +
                name + "': It is already registered for name '" + registeredName + "'");
        }
        
        aliasMap.put(alias, name);
        logger.debug("Registered alias '{}' for name '{}'", alias, name);
    }
    
    @Override
    public void removeAlias(String alias) {
        String name = aliasMap.remove(alias);
        if (name != null) {
            logger.debug("Removed alias '{}' for name '{}'", alias, name);
        }
    }
    
    @Override
    public boolean isAlias(String name) {
        return aliasMap.containsKey(name);
    }
    
    @Override
    public String[] getAliases(String name) {
        List<String> aliases = new ArrayList<>();
        for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
            if (entry.getValue().equals(name)) {
                aliases.add(entry.getKey());
            }
        }
        return aliases.toArray(new String[0]);
    }
    
    /**
     * 检查是否存在别名循环引用
     *
     * @param name 要注册的bean名称
     * @param alias 要注册的别名
     * @return 如果存在循环引用返回true，否则返回false
     */
    protected boolean hasAliasCycle(String name, String alias) {
        String registeredName = aliasMap.get(name);
        while (registeredName != null) {
            if (registeredName.equals(alias)) {
                return true;
            }
            registeredName = aliasMap.get(registeredName);
        }
        return false;
    }
    
    /**
     * 解析bean的规范名称（如果是别名，返回对应的bean名称）
     *
     * @param name 要解析的名称
     * @return 规范名称
     */
    public String canonicalName(String name) {
        String canonicalName = name;
        String resolvedName;
        
        // 循环解析别名，直到找到最终的bean名称
        do {
            resolvedName = aliasMap.get(canonicalName);
            if (resolvedName != null) {
                canonicalName = resolvedName;
            }
        } while (resolvedName != null);
        
        return canonicalName;
    }
} 