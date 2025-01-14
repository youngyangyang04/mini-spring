package com.kama.minispring.cloud.loadbalancer;

import com.kama.minispring.cloud.registry.ServiceInstance;
import com.kama.minispring.cloud.registry.ServiceStatus;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡器实现
 * 
 * @author kama
 * @version 1.0.0
 */
public class RoundRobinLoadBalancer implements LoadBalancer {
    
    private final AtomicInteger position = new AtomicInteger(0);
    private static final String NAME = "RoundRobinLoadBalancer";
    
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
        
        // 轮询选择一个实例
        int pos = position.getAndIncrement();
        if (pos >= Integer.MAX_VALUE) {
            position.set(0);
            pos = 0;
        }
        return availableInstances.get(pos % availableInstances.size());
    }
    
    @Override
    public String getName() {
        return NAME;
    }
} 