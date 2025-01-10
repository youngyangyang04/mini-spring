package com.kama.minispring.core.io;

import com.kama.minispring.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * URL资源实现类
 * 支持访问任何可以通过URL访问的资源，包括HTTP、HTTPS、FTP等
 *
 * @author kama
 * @version 1.0.0
 */
public class UrlResource implements Resource {
    
    private static final Logger logger = LoggerFactory.getLogger(UrlResource.class);
    
    private final URL url;
    
    /**
     * 通过URL字符串创建资源
     *
     * @param url URL字符串
     * @throws MalformedURLException 如果URL格式不正确
     */
    public UrlResource(String url) throws MalformedURLException {
        Assert.notNull(url, "URL must not be null");
        this.url = new URL(url);
    }
    
    /**
     * 通过URL对象创建资源
     *
     * @param url URL对象
     */
    public UrlResource(URL url) {
        Assert.notNull(url, "URL must not be null");
        this.url = url;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        URLConnection con = this.url.openConnection();
        try {
            return con.getInputStream();
        } catch (IOException ex) {
            // 如果是HTTP连接，尝试断开连接
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
            throw ex;
        }
    }
    
    @Override
    public boolean exists() {
        try {
            URLConnection con = this.url.openConnection();
            if (con instanceof HttpURLConnection) {
                HttpURLConnection httpCon = (HttpURLConnection) con;
                httpCon.setRequestMethod("HEAD");
                int code = httpCon.getResponseCode();
                httpCon.disconnect();
                return (code >= 200 && code < 300);
            }
            
            // 对于非HTTP URL，尝试获取输入流
            try (InputStream is = con.getInputStream()) {
                return true;
            }
        } catch (IOException ex) {
            logger.debug("Failed to check existence of {}: {}", this.url, ex.getMessage());
            return false;
        }
    }
    
    @Override
    public String getDescription() {
        return "URL [" + this.url + "]";
    }
    
    @Override
    public String getFilename() {
        String path = url.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
    
    @Override
    public boolean isReadable() {
        return true;
    }
    
    @Override
    public long lastModified() throws IOException {
        URLConnection con = this.url.openConnection();
        try {
            return con.getLastModified();
        } finally {
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
        }
    }
    
    /**
     * 获取URL对象
     *
     * @return URL对象
     */
    public URL getURL() {
        return this.url;
    }
    
    /**
     * 创建相对于此URL的新资源
     *
     * @param relativePath 相对路径
     * @return 新的URL资源
     * @throws MalformedURLException 如果无法创建新的URL
     */
    public UrlResource createRelative(String relativePath) throws MalformedURLException {
        return new UrlResource(new URL(this.url, relativePath));
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UrlResource)) {
            return false;
        }
        return this.url.equals(((UrlResource) obj).url);
    }
    
    @Override
    public int hashCode() {
        return this.url.hashCode();
    }
} 