package com.kama.minispring.boot.autoconfigure;

import com.kama.minispring.beans.factory.support.BeanDefinitionRegistry;
import com.kama.minispring.core.env.Environment;
import com.kama.minispring.core.io.Resource;
import com.kama.minispring.core.io.ResourceLoader;
import com.kama.minispring.core.io.UrlResource;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * 自动配置加载器，负责加载和处理自动配置类
 * 
 * @author kama
 * @version 1.0.0
 */
public class AutoConfigurationLoader {
    
    private static final String AUTO_CONFIGURATION_LOCATION = "META-INF/spring.factories";
    private static final String AUTO_CONFIGURATION_KEY = "com.kama.minispring.boot.autoconfigure.AutoConfiguration";
    
    private final ResourceLoader resourceLoader;
    private final BeanDefinitionRegistry registry;
    private final Environment environment;
    
    public AutoConfigurationLoader(ResourceLoader resourceLoader, BeanDefinitionRegistry registry) {
        this(resourceLoader, registry, null);
    }
    
    public AutoConfigurationLoader(ResourceLoader resourceLoader, BeanDefinitionRegistry registry, Environment environment) {
        this.resourceLoader = resourceLoader;
        this.registry = registry;
        this.environment = environment;
    }
    
    /**
     * 加载自动配置类
     *
     * @return 加载的自动配置类列表
     */
    public List<Class<?>> loadAutoConfigurations() {
        List<Class<?>> configurations = new ArrayList<>();
        try {
            // 加载spring.factories文件
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(AUTO_CONFIGURATION_LOCATION);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                Resource resource = new UrlResource(url);
                Properties properties = new Properties();
                properties.load(resource.getInputStream());
                
                // 获取自动配置类
                String value = properties.getProperty(AUTO_CONFIGURATION_KEY);
                if (value != null && !value.isEmpty()) {
                    String[] classNames = value.split(",");
                    for (String className : classNames) {
                        try {
                            Class<?> clazz = Class.forName(className.trim());
                            if (AutoConfiguration.class.isAssignableFrom(clazz)) {
                                configurations.add(clazz);
                            }
                        } catch (ClassNotFoundException e) {
                            // 忽略找不到的类
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load auto-configurations", e);
        }
        return configurations;
    }
    
    /**
     * 处理自动配置类
     *
     * @param configurations 自动配置类列表
     */
    public void processAutoConfigurations(List<Class<?>> configurations) {
        for (Class<?> configuration : configurations) {
            try {
                Object instance = configuration.getDeclaredConstructor().newInstance();
                if (instance instanceof AutoConfiguration) {
                    // 如果配置类需要Environment，注入它
                    if (environment != null && instance.getClass().isAnnotationPresent(RequiresEnvironment.class)) {
                        injectEnvironment(instance);
                    }
                    ((AutoConfiguration) instance).configure();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to process auto-configuration: " + configuration, e);
            }
        }
    }
    
    private void injectEnvironment(Object instance) {
        try {
            var field = instance.getClass().getDeclaredField("environment");
            field.setAccessible(true);
            field.set(instance, environment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject environment", e);
        }
    }
} 