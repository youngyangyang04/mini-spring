package com.kama.minispring.core.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件系统资源加载的测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class FileSystemResourceTest {
    
    @TempDir
    Path tempDir;
    
    private File testFile;
    private FileSystemResource resource;
    
    @BeforeEach
    void setUp() throws IOException {
        // 创建测试文件
        testFile = tempDir.resolve("test.txt").toFile();
        Files.write(testFile.toPath(), "Hello, World!".getBytes(StandardCharsets.UTF_8));
        resource = new FileSystemResource(testFile);
    }
    
    @Test
    void testGetInputStream() throws IOException {
        // 测试读取文件内容
        try (var is = resource.getInputStream()) {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("Hello, World!", content);
        }
    }
    
    @Test
    void testExists() {
        // 测试文件存在性检查
        assertTrue(resource.exists());
        
        // 测试不存在的文件
        FileSystemResource nonExistingResource = new FileSystemResource(tempDir.resolve("non-existing.txt").toString());
        assertFalse(nonExistingResource.exists());
    }
    
    @Test
    void testGetDescription() {
        // 测试资源描述
        String description = resource.getDescription();
        assertTrue(description.startsWith("file ["));
        assertTrue(description.endsWith("test.txt]"));
    }
    
    @Test
    void testGetFilename() {
        // 测试获取文件名
        assertEquals("test.txt", resource.getFilename());
    }
    
    @Test
    void testIsReadable() {
        // 测试文件可读性
        assertTrue(resource.isReadable());
        
        // 创建一个新的只读文件来测试
        try {
            File readOnlyFile = tempDir.resolve("readonly.txt").toFile();
            Files.write(readOnlyFile.toPath(), "Read Only Content".getBytes(StandardCharsets.UTF_8));
            readOnlyFile.setReadOnly();  // 使用setReadOnly()而不是setReadable(false)
            
            FileSystemResource readOnlyResource = new FileSystemResource(readOnlyFile);
            assertTrue(readOnlyResource.exists());
            assertTrue(readOnlyResource.isReadable());  // 在Windows中，只读文件仍然是可读的
            
            // 清理
            readOnlyFile.setWritable(true);
            Files.delete(readOnlyFile.toPath());
        } catch (IOException e) {
            fail("Failed to create or manipulate read-only file: " + e.getMessage());
        }
    }
    
    @Test
    void testLastModified() throws IOException {
        // 测试最后修改时间
        long lastModified = resource.lastModified();
        assertTrue(lastModified > 0);
        
        // 测试不存在的文件
        FileSystemResource nonExistingResource = new FileSystemResource(tempDir.resolve("non-existing.txt").toString());
        assertThrows(IOException.class, nonExistingResource::lastModified);
    }
    
    @Test
    void testGetAbsolutePath() {
        // 测试获取绝对路径
        String absolutePath = resource.getAbsolutePath();
        assertTrue(absolutePath.endsWith("test.txt"));
        assertTrue(new File(absolutePath).exists());
    }
    
    @Test
    void testGetFile() {
        // 测试获取File对象
        File file = resource.getFile();
        assertEquals(testFile, file);
        assertTrue(file.exists());
    }
    
    @Test
    void testCreateRelative() {
        // 测试创建相对路径资源
        FileSystemResource relativeResource = resource.createRelative("relative.txt");
        assertEquals("relative.txt", relativeResource.getFilename());
        assertFalse(relativeResource.exists());
    }
    
    @Test
    void testNonExistentFile() {
        // 测试不存在文件的异常处理
        FileSystemResource nonExistingResource = new FileSystemResource(tempDir.resolve("non-existing.txt").toString());
        assertThrows(IOException.class, nonExistingResource::getInputStream);
    }
    
    @Test
    void testConstructorWithNullArguments() {
        // 测试构造函数参数验证
        assertThrows(IllegalArgumentException.class, () -> new FileSystemResource((String) null));
        assertThrows(IllegalArgumentException.class, () -> new FileSystemResource((File) null));
    }
} 