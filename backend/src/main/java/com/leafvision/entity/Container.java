package com.leafvision.entity;

import java.util.HashMap;
import java.util.Map;

public class Container {
    private String id;
    private String name;
    private String image;
    private String status;
    private String state;
    private String ports;
    private Long serverId;
    private String serverName;
    private Map<String, Object> metrics = new HashMap<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPorts() { return ports; }
    public void setPorts(String ports) { this.ports = ports; }
    public Long getServerId() { return serverId; }
    public void setServerId(Long serverId) { this.serverId = serverId; }
    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }
    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
}
