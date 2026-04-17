package com.leafvision.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Trace {
    private Long id;
    private String traceId;
    private String service;
    private String operation;
    private Long duration;
    private Integer spanCount;
    private String status;
    private String startTime;
    private Long serverId;
    private String serverName;
    private List<Map<String, Object>> spans;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    public Integer getSpanCount() { return spanCount; }
    public void setSpanCount(Integer spanCount) { this.spanCount = spanCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public Long getServerId() { return serverId; }
    public void setServerId(Long serverId) { this.serverId = serverId; }
    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }
    public List<Map<String, Object>> getSpans() { return spans; }
    public void setSpans(List<Map<String, Object>> spans) { this.spans = spans; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
