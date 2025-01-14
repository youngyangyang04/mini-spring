package com.kama.minispring.cloud.registry;

import java.net.URI;

/**
 * 服务实例，表示一个服务的具体实例
 * 
 * @author kama
 * @version 1.0.0
 */
public class ServiceInstance {
    private final String serviceName;
    private final String instanceId;
    private final URI uri;
    private final ServiceMetadata metadata;
    private ServiceStatus status;
    
    public ServiceInstance(String serviceName, String instanceId, URI uri, ServiceMetadata metadata) {
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.uri = uri;
        this.metadata = metadata;
        this.status = ServiceStatus.UP;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public URI getUri() {
        return uri;
    }
    
    public ServiceMetadata getMetadata() {
        return metadata;
    }
    
    public ServiceStatus getStatus() {
        return status;
    }
    
    public void setStatus(ServiceStatus status) {
        this.status = status;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstance that = (ServiceInstance) o;
        return serviceName.equals(that.serviceName) && instanceId.equals(that.instanceId);
    }
    
    @Override
    public int hashCode() {
        return 31 * serviceName.hashCode() + instanceId.hashCode();
    }
} 