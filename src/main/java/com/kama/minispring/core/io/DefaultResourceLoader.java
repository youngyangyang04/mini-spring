package com.kama.minispring.core.io;

import com.kama.minispring.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * ResourceLoader接口的默认实现
 * 可以加载类路径资源和URL资源
 *
 * @author kama
 * @version 1.0.0
 */
public class DefaultResourceLoader implements ResourceLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultResourceLoader.class);
    
    private ClassLoader classLoader;
    
    public DefaultResourceLoader() {
        this.classLoader = getClass().getClassLoader();
    }
    
    public DefaultResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader : getClass().getClassLoader();
    }
    
    @Override
    public Resource getResource(String location) {
        Assert.notNull(location, "Location must not be null");
        
        if (location.startsWith(CLASSPATH_URL_PREFIX)) {
            // 类路径资源
            return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
        }
        
        try {
            // 尝试作为URL
            URL url = new URL(location);
            return new UrlResource(url);
        } catch (MalformedURLException ex) {
            // 作为文件系统路径
            return new FileSystemResource(location);
        }
    }
    
    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
    
    /**
     * 设置类加载器
     *
     * @param classLoader 类加载器
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
} 