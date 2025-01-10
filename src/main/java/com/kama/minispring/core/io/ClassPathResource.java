package com.kama.minispring.core.io;

import com.kama.minispring.util.Assert;
import com.kama.minispring.util.ClassUtils;
import com.kama.minispring.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * 类路径资源实现类
 * 用于加载类路径下的资源文件
 *
 * @author kama
 * @version 1.0.0
 */
public class ClassPathResource implements Resource {
    
    private static final Logger logger = LoggerFactory.getLogger(ClassPathResource.class);
    
    private final String path;
    private final ClassLoader classLoader;
    private final Class<?> clazz;
    
    /**
     * 创建一个类路径资源
     *
     * @param path 资源路径
     */
    public ClassPathResource(String path) {
        this(path, (ClassLoader) null);
    }
    
    /**
     * 创建一个类路径资源
     *
     * @param path 资源路径
     * @param classLoader 类加载器
     */
    public ClassPathResource(String path, ClassLoader classLoader) {
        Assert.notNull(path, "Path must not be null");
        this.path = StringUtils.cleanPath(path);
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
        this.clazz = null;
    }
    
    /**
     * 创建一个类路径资源
     *
     * @param path 资源路径
     * @param clazz 所属类
     */
    public ClassPathResource(String path, Class<?> clazz) {
        Assert.notNull(path, "Path must not be null");
        this.path = StringUtils.cleanPath(path);
        this.clazz = clazz;
        this.classLoader = null;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        InputStream is;
        if (this.clazz != null) {
            String pathToUse = this.path;
            if (!pathToUse.startsWith("/")) {
                pathToUse = "/" + pathToUse;
            }
            is = this.clazz.getResourceAsStream(pathToUse);
        } else if (this.classLoader != null) {
            is = this.classLoader.getResourceAsStream(this.path);
        } else {
            is = ClassLoader.getSystemResourceAsStream(this.path);
        }
        
        if (is == null) {
            throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
        }
        
        logger.debug("Opened InputStream for {}", getDescription());
        return is;
    }
    
    @Override
    public boolean exists() {
        URL url = null;
        if (this.clazz != null) {
            url = this.clazz.getResource(this.path);
        } else if (this.classLoader != null) {
            url = this.classLoader.getResource(this.path);
        } else {
            url = ClassLoader.getSystemResource(this.path);
        }
        return url != null;
    }
    
    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder("class path resource [");
        if (this.clazz != null) {
            builder.append(this.clazz.getName()).append('/');
        }
        builder.append(this.path).append(']');
        return builder.toString();
    }
    
    @Override
    public String getFilename() {
        return StringUtils.getFilename(this.path);
    }
    
    @Override
    public boolean isReadable() {
        return exists();
    }
    
    @Override
    public long lastModified() throws IOException {
        URL url = null;
        if (this.clazz != null) {
            url = this.clazz.getResource(this.path);
        } else if (this.classLoader != null) {
            url = this.classLoader.getResource(this.path);
        } else {
            url = ClassLoader.getSystemResource(this.path);
        }
        
        if (url == null) {
            throw new FileNotFoundException(getDescription() + " cannot be resolved in the file system for resolving its last-modified timestamp");
        }
        
        try {
            return url.openConnection().getLastModified();
        } catch (IOException ex) {
            logger.debug("Could not get last-modified timestamp for {}: {}", getDescription(), ex.getMessage());
            throw ex;
        }
    }
    
    /**
     * 获取资源路径
     *
     * @return 资源路径
     */
    public String getPath() {
        return this.path;
    }
    
    /**
     * 获取类加载器
     *
     * @return 类加载器
     */
    public ClassLoader getClassLoader() {
        return (this.clazz != null ? this.clazz.getClassLoader() : this.classLoader);
    }
} 