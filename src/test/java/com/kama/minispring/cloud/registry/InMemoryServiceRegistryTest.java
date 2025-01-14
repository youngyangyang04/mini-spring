package com.kama.minispring.cloud.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 内存服务注册表测试类
 * 
 * @author kama
 * @version 1.0.0
 */
class InMemoryServiceRegistryTest {
    
    private ServiceRegistry registry;
    private static final String SERVICE_NAME = "test-service";
    private static final String INSTANCE_ID = "instance-1";
    private static final URI SERVICE_URI = URI.create("http://localhost:8080");
    
    @BeforeEach
    void setUp() {
        registry = new InMemoryServiceRegistry();
    }
    
    @Test
    void shouldRegisterService() {
        // 准备测试数据
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("version", "1.0.0");
        ServiceMetadata metadata = new ServiceMetadata(metadataMap);
        
        // 注册服务
        registry.register(SERVICE_NAME, INSTANCE_ID, SERVICE_URI, metadata);
        
        // 验证注册结果
        List<ServiceInstance> instances = registry.getInstances(SERVICE_NAME);
        assertEquals(1, instances.size());
        ServiceInstance instance = instances.get(0);
        assertEquals(SERVICE_NAME, instance.getServiceName());
        assertEquals(INSTANCE_ID, instance.getInstanceId());
        assertEquals(SERVICE_URI, instance.getUri());
        assertEquals("1.0.0", instance.getMetadata().getMetadata("version"));
        assertEquals(ServiceStatus.UP, instance.getStatus());
    }
    
    @Test
    void shouldDeregisterService() {
        // 准备测试数据
        ServiceMetadata metadata = new ServiceMetadata();
        registry.register(SERVICE_NAME, INSTANCE_ID, SERVICE_URI, metadata);
        
        // 注销服务
        registry.deregister(SERVICE_NAME, INSTANCE_ID);
        
        // 验证注销结果
        List<ServiceInstance> instances = registry.getInstances(SERVICE_NAME);
        assertTrue(instances.isEmpty());
        List<String> services = registry.getServices();
        assertTrue(services.isEmpty());
    }
    
    @Test
    void shouldUpdateServiceStatus() {
        // 准备测试数据
        ServiceMetadata metadata = new ServiceMetadata();
        registry.register(SERVICE_NAME, INSTANCE_ID, SERVICE_URI, metadata);
        
        // 更新服务状态
        registry.updateStatus(SERVICE_NAME, INSTANCE_ID, ServiceStatus.DOWN);
        
        // 验证状态更新
        List<ServiceInstance> instances = registry.getInstances(SERVICE_NAME);
        assertEquals(1, instances.size());
        assertEquals(ServiceStatus.DOWN, instances.get(0).getStatus());
    }
    
    @Test
    void shouldGetAllServices() {
        // 准备测试数据
        ServiceMetadata metadata = new ServiceMetadata();
        registry.register(SERVICE_NAME, INSTANCE_ID, SERVICE_URI, metadata);
        registry.register("another-service", "instance-2", URI.create("http://localhost:8081"), metadata);
        
        // 获取所有服务
        List<String> services = registry.getServices();
        
        // 验证结果
        assertEquals(2, services.size());
        assertTrue(services.contains(SERVICE_NAME));
        assertTrue(services.contains("another-service"));
    }
    
    @Test
    void shouldHandleNonExistentService() {
        // 验证不存在的服务
        List<ServiceInstance> instances = registry.getInstances("non-existent");
        assertTrue(instances.isEmpty());
        
        // 验证注销不存在的服务不会抛出异常
        assertDoesNotThrow(() -> registry.deregister("non-existent", "non-existent"));
        
        // 验证更新不存在的服务状态不会抛出异常
        assertDoesNotThrow(() -> 
            registry.updateStatus("non-existent", "non-existent", ServiceStatus.DOWN));
    }
} 