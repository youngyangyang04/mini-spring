package com.kama.minispring.core.io;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 类路径资源加载的测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class ClassPathResourceTest {
    
    @Test
    void testGetInputStream() throws IOException {
        // 创建一个类路径资源
        ClassPathResource resource = new ClassPathResource("test.txt");
        
        // 获取输入流并读取内容
        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            assertEquals("Hello, World!", content.trim());
        }
    }
    
    @Test
    void testGetInputStreamWithClassLoader() throws IOException {
        // 使用指定的类加载器创建类路径资源
        ClassPathResource resource = new ClassPathResource("test.txt", getClass().getClassLoader());
        
        // 获取输入流并读取内容
        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            assertEquals("Hello, World!", content.trim());
        }
    }
    
    @Test
    void testGetInputStreamWithClass() throws IOException {
        // 使用指定的类创建类路径资源
        ClassPathResource resource = new ClassPathResource("test.txt", getClass());
        
        // 获取输入流并读取内容
        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            assertEquals("Hello, World!", content.trim());
        }
    }
    
    @Test
    void testExists() {
        // 测试存在的资源
        ClassPathResource existingResource = new ClassPathResource("test.txt");
        assertTrue(existingResource.exists());
        
        // 测试不存在的资源
        ClassPathResource nonExistingResource = new ClassPathResource("non-existing.txt");
        assertFalse(nonExistingResource.exists());
    }
    
    @Test
    void testGetFilename() {
        // 测试简单文件名
        ClassPathResource resource1 = new ClassPathResource("test.txt");
        assertEquals("test.txt", resource1.getFilename());
        
        // 测试带路径的文件名
        ClassPathResource resource2 = new ClassPathResource("path/to/test.txt");
        assertEquals("test.txt", resource2.getFilename());
    }
    
    @Test
    void testGetDescription() {
        // 测试普通描述
        ClassPathResource resource1 = new ClassPathResource("test.txt");
        assertEquals("class path resource [test.txt]", resource1.getDescription());
        
        // 测试带类的描述
        ClassPathResource resource2 = new ClassPathResource("test.txt", getClass());
        assertTrue(resource2.getDescription().contains(getClass().getName()));
        assertTrue(resource2.getDescription().contains("test.txt"));
    }
    
    @Test
    void testIsReadable() {
        // 测试可读资源
        ClassPathResource readableResource = new ClassPathResource("test.txt");
        assertTrue(readableResource.isReadable());
        
        // 测试不可读资源
        ClassPathResource nonReadableResource = new ClassPathResource("non-existing.txt");
        assertFalse(nonReadableResource.isReadable());
    }
    
    @Test
    void testLastModified() {
        // 测试获取最后修改时间
        ClassPathResource resource = new ClassPathResource("test.txt");
        assertDoesNotThrow(() -> {
            long lastModified = resource.lastModified();
            assertTrue(lastModified > 0);
        });
        
        // 测试不存在的资源
        ClassPathResource nonExistingResource = new ClassPathResource("non-existing.txt");
        assertThrows(IOException.class, nonExistingResource::lastModified);
    }
} 