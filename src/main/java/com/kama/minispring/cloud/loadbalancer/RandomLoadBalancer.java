package com.kama.minispring.cloud.loadbalancer;

import com.kama.minispring.cloud.registry.ServiceInstance;
import com.kama.minispring.cloud.registry.ServiceStatus;
import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡器实现
 * 
 * @author kama
 * @version 1.0.0
 */
public class RandomLoadBalancer implements LoadBalancer {
    
    private final Random random = new Random();
    private static final String NAME = "RandomLoadBalancer";
    
    @Override
    public ServiceInstance choose(List<ServiceInstance> instances) {
        if (instances == null || instances.isEmpty()) {
            throw new IllegalArgumentException("Instance list cannot be empty");
        }
        
        // 过滤出状态为UP的实例
        List<ServiceInstance> availableInstances = instances.stream()
            .filter(instance -> instance.getStatus() == ServiceStatus.UP)
            .toList();
            
        if (availableInstances.isEmpty()) {
            throw new IllegalStateException("No available service instances");
        }
        
        // 随机选择一个实例
        int index = random.nextInt(availableInstances.size());
        return availableInstances.get(index);
    }
    
    @Override
    public String getName() {
        return NAME;
    }
} 