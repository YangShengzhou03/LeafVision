package com.leafvision.entity;

import java.util.HashMap;
import java.util.Map;

public class Service {
    private Long id;
    private String name;
    private String type;
    private String address;
    private Integer port;
    private String status;
    private Long serverId;
    private String serverName;
    private Integer instanceCount;
    private Integer healthyCount;
    private Double responseTime;
    private Double errorRate;
    private Map<String, Object> metrics = new HashMap<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getServerId() { return serverId; }
    public void setServerId(Long serverId) { this.serverId = serverId; }
    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }
    public Integer getInstanceCount() { return instanceCount; }
    public void setInstanceCount(Integer instanceCount) { this.instanceCount = instanceCount; }
    public Integer getHealthyCount() { return healthyCount; }
    public void setHealthyCount(Integer healthyCount) { this.healthyCount = healthyCount; }
    public Double getResponseTime() { return responseTime; }
    public void setResponseTime(Double responseTime) { this.responseTime = responseTime; }
    public Double getErrorRate() { return errorRate; }
    public void setErrorRate(Double errorRate) { this.errorRate = errorRate; }
    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
}
