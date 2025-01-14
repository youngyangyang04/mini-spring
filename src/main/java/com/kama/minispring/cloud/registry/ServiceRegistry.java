package com.kama.minispring.cloud.registry;

import java.net.URI;
import java.util.List;

/**
 * 服务注册接口，定义服务注册和发现的核心功能
 * 
 * @author kama
 * @version 1.0.0
 */
public interface ServiceRegistry {
    
    /**
     * 注册服务实例
     *
     * @param serviceName 服务名称
     * @param instanceId 实例ID
     * @param uri 服务URI
     * @param metadata 服务元数据
     */
    void register(String serviceName, String instanceId, URI uri, ServiceMetadata metadata);
    
    /**
     * 注销服务实例
     *
     * @param serviceName 服务名称
     * @param instanceId 实例ID
     */
    void deregister(String serviceName, String instanceId);
    
    /**
     * 获取服务实例列表
     *
     * @param serviceName 服务名称
     * @return 服务实例列表
     */
    List<ServiceInstance> getInstances(String serviceName);
    
    /**
     * 获取所有服务名称
     *
     * @return 服务名称列表
     */
    List<String> getServices();
    
    /**
     * 更新服务实例状态
     *
     * @param serviceName 服务名称
     * @param instanceId 实例ID
     * @param status 服务状态
     */
    void updateStatus(String serviceName, String instanceId, ServiceStatus status);
} 