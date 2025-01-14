package com.kama.minispring.core.env;

import java.util.*;

/**
 * 标准环境配置实现类
 * 
 * @author kama
 * @version 1.0.0
 */
public class StandardEnvironment implements Environment {
    
    private final Map<String, String> properties = new HashMap<>();
    private final List<String> activeProfiles = new ArrayList<>();
    private final List<String> defaultProfiles = new ArrayList<>();
    
    public StandardEnvironment() {
        // 加载系统属性
        properties.putAll(System.getenv());
        
        // 加载JVM属性
        Properties systemProperties = System.getProperties();
        systemProperties.forEach((key, value) -> properties.put(key.toString(), value.toString()));
        
        // 设置默认的配置文件
        defaultProfiles.add("default");
    }
    
    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }
    
    @Override
    public String getProperty(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }
    
    @Override
    public boolean containsProperty(String key) {
        return properties.containsKey(key);
    }
    
    @Override
    public String[] getActiveProfiles() {
        return activeProfiles.toArray(new String[0]);
    }
    
    @Override
    public String[] getDefaultProfiles() {
        return defaultProfiles.toArray(new String[0]);
    }
    
    @Override
    public boolean acceptsProfiles(String... profiles) {
        if (profiles == null || profiles.length == 0) {
            return false;
        }
        
        Set<String> activeProfileSet = new HashSet<>(activeProfiles);
        for (String profile : profiles) {
            if (activeProfileSet.contains(profile)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 设置激活的配置文件
     *
     * @param profiles 配置文件名称数组
     */
    public void setActiveProfiles(String... profiles) {
        activeProfiles.clear();
        if (profiles != null) {
            Collections.addAll(activeProfiles, profiles);
        }
    }
    
    /**
     * 添加激活的配置文件
     *
     * @param profile 配置文件名称
     */
    public void addActiveProfile(String profile) {
        if (profile != null) {
            activeProfiles.add(profile);
        }
    }
    
    /**
     * 设置属性值
     *
     * @param key 属性键
     * @param value 属性值
     */
    public void setProperty(String key, String value) {
        if (key != null) {
            properties.put(key, value);
        }
    }
} 