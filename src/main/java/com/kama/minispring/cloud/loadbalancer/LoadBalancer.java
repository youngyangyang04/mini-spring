package com.kama.minispring.cloud.loadbalancer;

import com.kama.minispring.cloud.registry.ServiceInstance;
import java.util.List;

/**
 * 负载均衡器接口，定义负载均衡的核心功能
 * 
 * @author kama
 * @version 1.0.0
 */
public interface LoadBalancer {
    
    /**
     * 从服务实例列表中选择一个实例
     *
     * @param instances 可用的服务实例列表
     * @return 选择的服务实例
     * @throws IllegalArgumentException 如果实例列表为空
     */
    ServiceInstance choose(List<ServiceInstance> instances);
    
    /**
     * 获取负载均衡器的名称
     *
     * @return 负载均衡器名称
     */
    String getName();
} 