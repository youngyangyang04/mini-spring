package com.kama.minispring.core.io;

import com.kama.minispring.util.Assert;
import com.kama.minispring.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件系统资源实现类
 * 用于加载文件系统中的资源文件
 *
 * @author kama
 * @version 1.0.0
 */
public class FileSystemResource implements Resource {
    
    private static final Logger logger = LoggerFactory.getLogger(FileSystemResource.class);
    
    private final String path;
    private final File file;
    
    /**
     * 通过路径创建文件系统资源
     *
     * @param path 资源路径
     */
    public FileSystemResource(String path) {
        Assert.notNull(path, "Path must not be null");
        this.path = StringUtils.cleanPath(path);
        this.file = new File(this.path);
    }
    
    /**
     * 通过File对象创建文件系统资源
     *
     * @param file 文件对象
     */
    public FileSystemResource(File file) {
        Assert.notNull(file, "File must not be null");
        this.path = StringUtils.cleanPath(file.getPath());
        this.file = file;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        try {
            InputStream is = Files.newInputStream(this.file.toPath());
            logger.debug("Opened InputStream for {}", getDescription());
            return is;
        } catch (IOException ex) {
            throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
        }
    }
    
    @Override
    public boolean exists() {
        return this.file.exists();
    }
    
    @Override
    public String getDescription() {
        return "file [" + this.path + "]";
    }
    
    @Override
    public String getFilename() {
        return this.file.getName();
    }
    
    @Override
    public boolean isReadable() {
        return this.file.canRead();
    }
    
    @Override
    public long lastModified() throws IOException {
        long lastModified = this.file.lastModified();
        if (lastModified == 0L && !this.file.exists()) {
            throw new FileNotFoundException(getDescription() +
                " cannot be resolved in the file system for resolving its last-modified timestamp");
        }
        return lastModified;
    }
    
    /**
     * 获取文件的绝对路径
     *
     * @return 绝对路径
     */
    public String getAbsolutePath() {
        return this.file.getAbsolutePath();
    }
    
    /**
     * 获取文件对象
     *
     * @return 文件对象
     */
    public File getFile() {
        return this.file;
    }
    
    /**
     * 创建相对于此资源的新资源
     *
     * @param relativePath 相对路径
     * @return 新的文件系统资源
     */
    public FileSystemResource createRelative(String relativePath) {
        String pathToUse = StringUtils.cleanPath(this.path);
        Path parent = Paths.get(pathToUse).getParent();
        if (parent != null) {
            return new FileSystemResource(parent.resolve(relativePath).toString());
        } else {
            return new FileSystemResource(relativePath);
        }
    }
} 