package com.leafvision.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leafvision.client.AlertmanagerClient;
import com.leafvision.client.PrometheusClient;
import com.leafvision.entity.Server;
import com.leafvision.exception.BusinessException;
import com.leafvision.mapper.ServerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServerService {

    private static final Logger log = LoggerFactory.getLogger(ServerService.class);

    private final ServerMapper serverMapper;
    private final PrometheusClient prometheusClient;
    private final AlertmanagerClient alertmanagerClient;

    public ServerService(ServerMapper serverMapper, 
                         PrometheusClient prometheusClient, 
                         AlertmanagerClient alertmanagerClient) {
        this.serverMapper = serverMapper;
        this.prometheusClient = prometheusClient;
        this.alertmanagerClient = alertmanagerClient;
    }

    public List<Server> getAllServers() {
        return serverMapper.selectList(null);
    }

    public Server getServerById(Long id) {
        return serverMapper.selectById(id);
    }

    public List<Server> getServersByType(String type) {
        return serverMapper.findByType(type);
    }

    @Transactional
    public Server addServer(Server server) {
        if (serverMapper.existsByIpAndPort(server.getIp(), server.getPort())) {
            throw new BusinessException("服务器已存在: " + server.getIp() + ":" + server.getPort());
        }
        server.setStatus("unknown");
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());
        serverMapper.insert(server);
        return server;
    }

    @Transactional
    public Server updateServer(Long id, Server server) {
        Server existing = serverMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("服务器不存在: " + id);
        }
        
        existing.setName(server.getName());
        existing.setIp(server.getIp());
        existing.setPort(server.getPort());
        existing.setType(server.getType());
        existing.setUpdatedAt(LocalDateTime.now());
        
        serverMapper.updateById(existing);
        return existing;
    }

    @Transactional
    public boolean deleteServer(Long id) {
        return serverMapper.deleteById(id) > 0;
    }

    @Transactional
    public void refreshStatus() {
        List<Server> servers = serverMapper.selectList(null);
        for (Server server : servers) {
            try {
                refreshServerStatus(server);
            } catch (Exception e) {
                log.error("Failed to refresh status for server: {}", server.getName(), e);
                server.setStatus("offline");
                server.setUpdatedAt(LocalDateTime.now());
                serverMapper.updateById(server);
            }
        }
    }

    @Transactional
    public void refreshServerStatus(Server server) {
        boolean isHealthy = false;
        
        switch (server.getType()) {
            case "prometheus-master":
            case "prometheus-node":
                isHealthy = prometheusClient.checkHealth(server.getIp(), server.getPort());
                if (isHealthy) {
                    fetchPrometheusMetrics(server);
                }
                break;
            case "alertmanager":
                isHealthy = alertmanagerClient.checkHealth(server.getIp(), server.getPort());
                break;
            default:
                log.warn("Unknown server type: {}", server.getType());
        }
        
        server.setStatus(isHealthy ? "online" : "offline");
        server.setLastSyncAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());
        serverMapper.updateById(server);
    }

    private void fetchPrometheusMetrics(Server server) {
        try {
            JSONObject cpuResult = prometheusClient.query(
                    server.getIp(), 
                    server.getPort(), 
                    "100 - (avg by(instance) (irate(node_cpu_seconds_total{mode=\"idle\"}[1m])) * 100)"
            );
            Double cpuUsage = prometheusClient.extractValueFromResult(cpuResult, "cpu");
            if (cpuUsage != null && !cpuUsage.isNaN()) {
                server.setCpuUsage(Math.round(cpuUsage * 10.0) / 10.0);
            }

            JSONObject memResult = prometheusClient.query(
                    server.getIp(), 
                    server.getPort(), 
                    "100 - ((node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes) * 100)"
            );
            Double memUsage = prometheusClient.extractValueFromResult(memResult, "memory");
            if (memUsage != null && !memUsage.isNaN()) {
                server.setMemoryUsage(Math.round(memUsage * 10.0) / 10.0);
            }

            JSONObject versionResult = prometheusClient.query(
                    server.getIp(), 
                    server.getPort(), 
                    "prometheus_build_info"
            );
            if (versionResult != null && "success".equals(versionResult.getString("status"))) {
                var data = versionResult.getJSONObject("data");
                var results = data.getJSONArray("result");
                if (results != null && !results.isEmpty()) {
                    var metric = results.getJSONObject(0).getJSONObject("metric");
                    if (metric != null) {
                        server.setVersion(metric.getString("version"));
                    }
                }
            }

            JSONObject uptimeResult = prometheusClient.query(
                    server.getIp(), 
                    server.getPort(), 
                    "time() - process_start_time_seconds"
            );
            Double uptimeSeconds = prometheusClient.extractValueFromResult(uptimeResult, "uptime");
            if (uptimeSeconds != null && !uptimeSeconds.isNaN() && uptimeSeconds > 0) {
                server.setUptime(formatUptime(uptimeSeconds.longValue()));
            }

        } catch (Exception e) {
            log.error("Failed to fetch Prometheus metrics for server: {}", server.getName(), e);
        }
    }

    private String formatUptime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        
        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    public void checkServerHealth(Server server) {
        boolean isHealthy = false;
        
        if ("prometheus-master".equals(server.getType()) || "prometheus-node".equals(server.getType())) {
            isHealthy = prometheusClient.checkHealth(server.getIp(), server.getPort());
        } else if ("alertmanager".equals(server.getType())) {
            isHealthy = alertmanagerClient.checkHealth(server.getIp(), server.getPort());
        }
        
        server.setStatus(isHealthy ? "online" : "offline");
    }
}
