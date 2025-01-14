package com.kama.minispring.cloud.loadbalancer;

import com.kama.minispring.cloud.registry.ServiceInstance;
import com.kama.minispring.cloud.registry.ServiceMetadata;
import com.kama.minispring.cloud.registry.ServiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 负载均衡器测试类
 * 
 * @author kama
 * @version 1.0.0
 */
class LoadBalancerTest {
    
    private List<ServiceInstance> instances;
    private LoadBalancer randomLoadBalancer;
    private LoadBalancer roundRobinLoadBalancer;
    
    @BeforeEach
    void setUp() {
        instances = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            ServiceInstance instance = new ServiceInstance(
                "test-service",
                "instance-" + i,
                URI.create("http://localhost:808" + i),
                new ServiceMetadata()
            );
            instances.add(instance);
        }
        
        randomLoadBalancer = new RandomLoadBalancer();
        roundRobinLoadBalancer = new RoundRobinLoadBalancer();
    }
    
    @Test
    void shouldChooseRandomInstance() {
        // 多次选择实例，验证是否有不同的选择结果
        Set<ServiceInstance> selectedInstances = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            selectedInstances.add(randomLoadBalancer.choose(instances));
        }
        
        // 验证是否有多个不同的选择结果
        assertTrue(selectedInstances.size() > 1);
    }
    
    @Test
    void shouldChooseInstancesInRoundRobin() {
        // 记录前三次选择的实例
        ServiceInstance first = roundRobinLoadBalancer.choose(instances);
        ServiceInstance second = roundRobinLoadBalancer.choose(instances);
        ServiceInstance third = roundRobinLoadBalancer.choose(instances);
        
        // 验证是否按顺序选择
        assertNotEquals(first, second);
        assertNotEquals(second, third);
        assertNotEquals(first, third);
        
        // 验证第四次选择是否回到第一个实例
        ServiceInstance fourth = roundRobinLoadBalancer.choose(instances);
        assertEquals(first, fourth);
    }
    
    @Test
    void shouldHandleEmptyInstanceList() {
        // 验证空列表抛出异常
        assertThrows(IllegalArgumentException.class, () -> 
            randomLoadBalancer.choose(Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> 
            roundRobinLoadBalancer.choose(Collections.emptyList()));
    }
    
    @Test
    void shouldHandleAllInstancesDown() {
        // 将所有实例状态设置为DOWN
        instances.forEach(instance -> instance.setStatus(ServiceStatus.DOWN));
        
        // 验证无可用实例时抛出异常
        assertThrows(IllegalStateException.class, () -> 
            randomLoadBalancer.choose(instances));
        assertThrows(IllegalStateException.class, () -> 
            roundRobinLoadBalancer.choose(instances));
    }
    
    @Test
    void shouldOnlyChooseUpInstances() {
        // 将部分实例设置为DOWN
        instances.get(0).setStatus(ServiceStatus.DOWN);
        instances.get(1).setStatus(ServiceStatus.DOWN);
        
        // 验证只选择UP状态的实例
        ServiceInstance chosen = randomLoadBalancer.choose(instances);
        assertEquals(ServiceStatus.UP, chosen.getStatus());
        
        chosen = roundRobinLoadBalancer.choose(instances);
        assertEquals(ServiceStatus.UP, chosen.getStatus());
    }
} 