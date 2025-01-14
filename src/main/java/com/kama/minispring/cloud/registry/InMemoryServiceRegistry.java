package com.kama.minispring.cloud.registry;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于内存的服务注册表实现
 * 
 * @author kama
 * @version 1.0.0
 */
public class InMemoryServiceRegistry implements ServiceRegistry {
    
    private final ConcurrentMap<String, Map<String, ServiceInstance>> services;
    
    public InMemoryServiceRegistry() {
        this.services = new ConcurrentHashMap<>();
    }
    
    @Override
    public void register(String serviceName, String instanceId, URI uri, ServiceMetadata metadata) {
        services.computeIfAbsent(serviceName, k -> new ConcurrentHashMap<>())
            .put(instanceId, new ServiceInstance(serviceName, instanceId, uri, metadata));
    }
    
    @Override
    public void deregister(String serviceName, String instanceId) {
        Map<String, ServiceInstance> serviceInstances = services.get(serviceName);
        if (serviceInstances != null) {
            serviceInstances.remove(instanceId);
            if (serviceInstances.isEmpty()) {
                services.remove(serviceName);
            }
        }
    }
    
    @Override
    public List<ServiceInstance> getInstances(String serviceName) {
        Map<String, ServiceInstance> serviceInstances = services.get(serviceName);
        if (serviceInstances == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(serviceInstances.values());
    }
    
    @Override
    public List<String> getServices() {
        return new ArrayList<>(services.keySet());
    }
    
    @Override
    public void updateStatus(String serviceName, String instanceId, ServiceStatus status) {
        Map<String, ServiceInstance> serviceInstances = services.get(serviceName);
        if (serviceInstances != null) {
            ServiceInstance instance = serviceInstances.get(instanceId);
            if (instance != null) {
                instance.setStatus(status);
            }
        }
    }
    
    /**
     * 获取服务实例数量
     *
     * @return 服务实例总数
     */
    public int getInstanceCount() {
        return services.values().stream()
            .mapToInt(Map::size)
            .sum();
    }
} 