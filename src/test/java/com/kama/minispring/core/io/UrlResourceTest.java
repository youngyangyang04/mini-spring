package com.kama.minispring.core.io;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * URL资源加载的测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class UrlResourceTest {
    
    private static final Logger logger = LoggerFactory.getLogger(UrlResourceTest.class);
    
    @TempDir
    Path tempDir;
    
    private File testFile;
    private UrlResource fileUrlResource;
    private final List<Closeable> resources = new ArrayList<>();
    
    @BeforeEach
    void setUp() throws IOException {
        // 创建测试文件
        testFile = tempDir.resolve("test.txt").toFile();
        Files.write(testFile.toPath(), "Hello, World!".getBytes(StandardCharsets.UTF_8));
        fileUrlResource = new UrlResource(testFile.toURI().toURL());
    }
    
    @AfterEach
    void tearDown() {
        // 关闭所有资源
        for (Closeable resource : resources) {
            try {
                resource.close();
                logger.debug("Successfully closed resource: {}", resource);
            } catch (IOException e) {
                logger.warn("Failed to close resource: {}", resource, e);
            }
        }
        resources.clear();
        
        // 强制执行GC和finalize
        System.gc();
        System.runFinalization();
        
        try {
            // 等待一小段时间让文件句柄释放
            Thread.sleep(100);
            
            // 删除文件
            if (testFile != null && testFile.exists()) {
                boolean deleted = Files.deleteIfExists(testFile.toPath());
                if (deleted) {
                    logger.debug("Successfully deleted test file: {}", testFile);
                } else {
                    logger.warn("Failed to delete test file: {}", testFile);
                }
            }
        } catch (Exception e) {
            logger.error("Error during cleanup", e);
        }
    }
    
    // 工具方法来跟踪资源
    private <T extends Closeable> T track(T resource) {
        resources.add(resource);
        logger.debug("Tracking new resource: {}", resource);
        return resource;
    }
    
    @Test
    void testGetInputStream() throws IOException {
        // 测试读取文件内容
        InputStream is = track(fileUrlResource.getInputStream());
        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("Hello, World!", content);
    }
    
    @Test
    void testExists() throws IOException {
        // 测试文件存在性检查
        assertTrue(fileUrlResource.exists());
        
        // 测试不存在的文件
        File nonExistingFile = tempDir.resolve("non-existing.txt").toFile();
        UrlResource nonExistingResource = new UrlResource(nonExistingFile.toURI().toURL());
        assertFalse(nonExistingResource.exists());
    }
    
    @Test
    void testGetDescription() {
        // 测试资源描述
        String description = fileUrlResource.getDescription();
        assertTrue(description.startsWith("URL [file:"));
        assertTrue(description.endsWith("test.txt]"));
    }
    
    @Test
    void testGetFilename() {
        // 测试获取文件名
        assertEquals("test.txt", fileUrlResource.getFilename());
    }
    
    @Test
    void testIsReadable() {
        // URL资源总是被认为是可读的
        assertTrue(fileUrlResource.isReadable());
    }
    
    @Test
    void testLastModified() throws IOException {
        // 测试最后修改时间
        long lastModified = fileUrlResource.lastModified();
        assertTrue(lastModified > 0);
        assertEquals(testFile.lastModified(), lastModified);
    }
    
    @Test
    void testCreateRelative() throws MalformedURLException {
        // 测试创建相对路径资源
        UrlResource relativeResource = fileUrlResource.createRelative("relative.txt");
        assertNotNull(relativeResource);
        assertEquals("relative.txt", relativeResource.getFilename());
    }
    
    @Test
    void testEqualsAndHashCode() throws MalformedURLException {
        // 测试equals和hashCode方法
        UrlResource resource1 = new UrlResource(testFile.toURI().toURL());
        UrlResource resource2 = new UrlResource(testFile.toURI().toURL());
        
        File otherFile = tempDir.resolve("other.txt").toFile();
        UrlResource resource3 = new UrlResource(otherFile.toURI().toURL());
        
        // 测试相等性
        assertEquals(resource1, resource2);
        assertNotEquals(resource1, resource3);
        
        // 测试哈希码
        assertEquals(resource1.hashCode(), resource2.hashCode());
        assertNotEquals(resource1.hashCode(), resource3.hashCode());
    }
    
    @Test
    void testConstructorWithNullArguments() {
        // 测试构造函数参数验证
        assertThrows(IllegalArgumentException.class, () -> new UrlResource((URL) null));
        assertThrows(IllegalArgumentException.class, () -> new UrlResource((String) null));
    }
    
    @Test
    void testMalformedUrl() {
        // 测试错误的URL格式
        assertThrows(MalformedURLException.class, () -> new UrlResource("invalid:url"));
    }
} 