package com.leafvision.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leafvision.client.PrometheusClient;
import com.leafvision.entity.Alert;
import com.leafvision.entity.DashboardData;
import com.leafvision.entity.Server;
import com.leafvision.mapper.AlertMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final ServerService serverService;
    private final PrometheusClient prometheusClient;
    private final AlertMapper alertMapper;

    public DashboardService(ServerService serverService,
                            PrometheusClient prometheusClient,
                            AlertMapper alertMapper) {
        this.serverService = serverService;
        this.prometheusClient = prometheusClient;
        this.alertMapper = alertMapper;
    }

    public DashboardData getDashboardData() {
        DashboardData data = new DashboardData();

        List<Server> servers = serverService.getAllServers();
        
        refreshServerMetrics(servers);

        List<Map<String, Object>> statsCards = calculateStats(servers);
        data.setStatsCards(statsCards);

        List<Double> cpuTrend = calculateCpuTrend(servers);
        data.setCpuTrend(cpuTrend);

        List<Double> memoryTrend = calculateMemoryTrend(servers);
        data.setMemoryTrend(memoryTrend);

        List<Map<String, Object>> networkDistribution = calculateNetworkDistribution(servers);
        data.setNetworkDistribution(networkDistribution);

        data.setServerList(servers);

        return data;
    }

    private void refreshServerMetrics(List<Server> servers) {
        for (Server server : servers) {
            if (!"online".equals(server.getStatus())) {
                continue;
            }
            
            try {
                JSONObject cpuResult = prometheusClient.query(
                    server.getIp(), server.getPort(),
                    "100 - (avg by(instance) (irate(node_cpu_seconds_total{mode=\"idle\"}[1m])) * 100)"
                );
                Double cpu = prometheusClient.extractValueFromResult(cpuResult, "cpu");
                if (cpu != null && !cpu.isNaN()) {
                    server.setCpuUsage(Math.round(cpu * 10.0) / 10.0);
                }
                
                JSONObject memResult = prometheusClient.query(
                    server.getIp(), server.getPort(),
                    "100 - ((node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes) * 100)"
                );
                Double memory = prometheusClient.extractValueFromResult(memResult, "memory");
                if (memory != null && !memory.isNaN()) {
                    server.setMemoryUsage(Math.round(memory * 10.0) / 10.0);
                }
                
                JSONObject versionResult = prometheusClient.query(
                    server.getIp(), server.getPort(),
                    "prometheus_build_info"
                );
                if (versionResult != null && "success".equals(versionResult.getString("status"))) {
                    JSONObject data = versionResult.getJSONObject("data");
                    JSONArray results = data.getJSONArray("result");
                    if (results != null && !results.isEmpty()) {
                        JSONObject labels = results.getJSONObject(0).getJSONObject("metric");
                        if (labels != null) {
                            server.setVersion(labels.getString("version"));
                        }
                    }
                }
                
                JSONObject uptimeResult = prometheusClient.query(
                    server.getIp(), server.getPort(),
                    "time() - process_start_time_seconds"
                );
                Double uptimeSeconds = prometheusClient.extractValueFromResult(uptimeResult, "uptime");
                if (uptimeSeconds != null && !uptimeSeconds.isNaN() && uptimeSeconds > 0) {
                    server.setUptime(formatUptime(uptimeSeconds.longValue()));
                } else {
                    server.setUptime("N/A");
                }
            } catch (Exception e) {
                log.warn("Failed to get metrics for server {}: {}", server.getName(), e.getMessage());
            }
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

    private List<Map<String, Object>> calculateStats(List<Server> servers) {
        List<Map<String, Object>> statsCards = new ArrayList<>();
        
        double totalCpu = 0;
        double totalMemory = 0;
        int onlineCount = 0;
        int activeAlerts = 0;
        
        for (Server server : servers) {
            if ("online".equals(server.getStatus())) {
                onlineCount++;
                if (server.getCpuUsage() != null) {
                    totalCpu += server.getCpuUsage();
                }
                if (server.getMemoryUsage() != null) {
                    totalMemory += server.getMemoryUsage();
                }
            }
        }
        
        QueryWrapper<Alert> alertWrapper = new QueryWrapper<>();
        alertWrapper.eq("status", "firing");
        activeAlerts = Math.toIntExact(alertMapper.selectCount(alertWrapper));
        
        double avgCpu = onlineCount > 0 ? totalCpu / onlineCount : 0;
        double avgMemory = onlineCount > 0 ? totalMemory / onlineCount : 0;
        
        statsCards.add(createStatCard("在线服务器", onlineCount + "/" + servers.size(), "server", "#67C23A"));
        statsCards.add(createStatCard("平均 CPU 使用率", String.format("%.1f%%", avgCpu), "cpu", "#409EFF"));
        statsCards.add(createStatCard("平均内存使用率", String.format("%.1f%%", avgMemory), "memory", "#E6A23C"));
        statsCards.add(createStatCard("活跃告警", String.valueOf(activeAlerts), "alert", activeAlerts > 0 ? "#F56C6C" : "#67C23A"));
        
        return statsCards;
    }

    private List<Double> calculateCpuTrend(List<Server> servers) {
        List<Double> trend = new ArrayList<>();
        
        Optional<Server> masterServer = servers.stream()
                .filter(s -> "prometheus-master".equals(s.getType()) && "online".equals(s.getStatus()))
                .findFirst();
        
        if (masterServer.isPresent()) {
            Server server = masterServer.get();
            long end = System.currentTimeMillis() / 1000;
            long start = end - 3600;
            
            JSONObject result = prometheusClient.queryRange(
                    server.getIp(), 
                    server.getPort(),
                    "100 - (avg by(instance) (irate(node_cpu_seconds_total{mode=\"idle\"}[1m])) * 100)",
                    start, end, "5m"
            );
            
            if (result != null && "success".equals(result.getString("status"))) {
                JSONObject data = result.getJSONObject("data");
                JSONArray results = data.getJSONArray("result");
                if (results != null && !results.isEmpty()) {
                    JSONArray values = results.getJSONObject(0).getJSONArray("values");
                    if (values != null) {
                        for (int i = 0; i < values.size(); i++) {
                            JSONArray point = values.getJSONArray(i);
                            Double val = point.getDouble(1);
                            trend.add(val != null && !val.isNaN() ? val : 0);
                        }
                    }
                }
            }
        }
        
        return trend;
    }

    private List<Double> calculateMemoryTrend(List<Server> servers) {
        List<Double> trend = new ArrayList<>();
        
        Optional<Server> masterServer = servers.stream()
                .filter(s -> "prometheus-master".equals(s.getType()) && "online".equals(s.getStatus()))
                .findFirst();
        
        if (masterServer.isPresent()) {
            Server server = masterServer.get();
            long end = System.currentTimeMillis() / 1000;
            long start = end - 3600;
            
            JSONObject result = prometheusClient.queryRange(
                    server.getIp(), 
                    server.getPort(),
                    "100 - ((node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes) * 100)",
                    start, end, "5m"
            );
            
            if (result != null && "success".equals(result.getString("status"))) {
                JSONObject data = result.getJSONObject("data");
                JSONArray results = data.getJSONArray("result");
                if (results != null && !results.isEmpty()) {
                    JSONArray values = results.getJSONObject(0).getJSONArray("values");
                    if (values != null) {
                        for (int i = 0; i < values.size(); i++) {
                            JSONArray point = values.getJSONArray(i);
                            Double val = point.getDouble(1);
                            trend.add(val != null && !val.isNaN() ? val : 0);
                        }
                    }
                }
            }
        }
        
        return trend;
    }

    private List<Map<String, Object>> calculateNetworkDistribution(List<Server> servers) {
        List<Map<String, Object>> distribution = new ArrayList<>();
        
        int prometheusCount = 0;
        int alertmanagerCount = 0;
        int onlineCount = 0;
        
        for (Server server : servers) {
            if ("online".equals(server.getStatus())) {
                onlineCount++;
                if (server.getType().startsWith("prometheus")) {
                    prometheusCount++;
                } else if ("alertmanager".equals(server.getType())) {
                    alertmanagerCount++;
                }
            }
        }
        
        distribution.add(createNetworkData("Prometheus 节点", prometheusCount));
        distribution.add(createNetworkData("Alertmanager", alertmanagerCount));
        distribution.add(createNetworkData("离线节点", servers.size() - onlineCount));
        
        return distribution;
    }

    private Map<String, Object> createStatCard(String title, String value, String type, String color) {
        Map<String, Object> card = new HashMap<>();
        card.put("title", title);
        card.put("value", value);
        card.put("type", type);
        card.put("color", color);
        return card;
    }

    private Map<String, Object> createNetworkData(String name, int value) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("value", value);
        return data;
    }
}
