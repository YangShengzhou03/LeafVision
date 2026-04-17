package com.leafvision.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leafvision.client.PrometheusClient;
import com.leafvision.entity.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MetricsService {

    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);

    private final PrometheusClient prometheusClient;
    private final ServerService serverService;

    public MetricsService(PrometheusClient prometheusClient, ServerService serverService) {
        this.prometheusClient = prometheusClient;
        this.serverService = serverService;
    }

    public Map<String, Object> queryMetrics(String metric, Long serverId, String timeRange) {
        Map<String, Object> response = new HashMap<>();
        
        Server server = serverService.getServerById(serverId);
        if (server == null) {
            response.put("status", "error");
            response.put("error", "Server not found");
            return response;
        }
        
        if (!"online".equals(server.getStatus())) {
            response.put("status", "error");
            response.put("error", "Server is offline");
            return response;
        }
        
        long end = System.currentTimeMillis() / 1000;
        long start;
        String step = "15s";
        
        if (timeRange == null || timeRange.isEmpty()) {
            timeRange = "1h";
        }
        
        switch (timeRange) {
            case "15m":
                start = end - 900;
                step = "15s";
                break;
            case "30m":
                start = end - 1800;
                step = "30s";
                break;
            case "1h":
                start = end - 3600;
                step = "1m";
                break;
            case "6h":
                start = end - 21600;
                step = "5m";
                break;
            case "12h":
                start = end - 43200;
                step = "10m";
                break;
            case "24h":
                start = end - 86400;
                step = "15m";
                break;
            case "7d":
                start = end - 604800;
                step = "1h";
                break;
            default:
                start = end - 3600;
                step = "1m";
        }
        
        JSONObject result = prometheusClient.queryRange(server.getIp(), server.getPort(), metric, start, end, step);
        
        if (result != null) {
            response.put("status", result.getString("status"));
            JSONObject data = result.getJSONObject("data");
            if (data != null) {
                JSONArray results = data.getJSONArray("result");
                if (results != null && !results.isEmpty()) {
                    JSONArray values = results.getJSONObject(0).getJSONArray("values");
                    List<Map<String, Object>> formattedValues = new ArrayList<>();
                    if (values != null) {
                        for (int i = 0; i < values.size(); i++) {
                            JSONArray point = values.getJSONArray(i);
                            Map<String, Object> pointMap = new HashMap<>();
                            pointMap.put("timestamp", point.getLong(0));
                            pointMap.put("value", point.getDouble(1));
                            formattedValues.add(pointMap);
                        }
                    }
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("values", formattedValues);
                    responseData.put("metric", data.get("result"));
                    response.put("data", responseData);
                } else {
                    response.put("data", new HashMap<>());
                }
            }
        } else {
            response.put("status", "error");
            response.put("error", "Failed to query Prometheus");
        }
        
        return response;
    }

    public Map<String, Object> queryMetricsRange(String metric, Long serverId, Long start, Long end, String step) {
        Map<String, Object> response = new HashMap<>();
        
        Server server = serverService.getServerById(serverId);
        if (server == null) {
            response.put("status", "error");
            response.put("error", "Server not found");
            return response;
        }
        
        if (!"online".equals(server.getStatus())) {
            response.put("status", "error");
            response.put("error", "Server is offline");
            return response;
        }
        
        if (step == null || step.isEmpty()) {
            step = "15s";
        }
        
        JSONObject result = prometheusClient.queryRange(server.getIp(), server.getPort(), metric, start, end, step);
        
        if (result != null) {
            response.put("status", result.getString("status"));
            response.put("data", result.get("data"));
        } else {
            response.put("status", "error");
            response.put("error", "Failed to query Prometheus");
        }
        
        return response;
    }

    public List<Map<String, Object>> getAvailableMetrics(Long serverId) {
        List<Map<String, Object>> metrics = new ArrayList<>();
        
        Server server = serverService.getServerById(serverId);
        if (server == null || !"online".equals(server.getStatus())) {
            return metrics;
        }
        
        JSONObject result = prometheusClient.getMetrics(server.getIp(), server.getPort());
        
        if (result != null && "success".equals(result.getString("status"))) {
            JSONObject data = result.getJSONObject("data");
            if (data != null) {
                for (String metricName : data.keySet()) {
                    Map<String, Object> metricInfo = new HashMap<>();
                    metricInfo.put("name", metricName);
                    
                    JSONArray metadata = data.getJSONArray(metricName);
                    if (metadata != null && !metadata.isEmpty()) {
                        JSONObject meta = metadata.getJSONObject(0);
                        metricInfo.put("type", meta.getString("type"));
                        metricInfo.put("help", meta.getString("help"));
                    }
                    
                    metrics.add(metricInfo);
                }
            }
        }
        
        return metrics;
    }

    public Map<String, Object> getInstantMetrics(Long serverId) {
        Map<String, Object> metrics = new HashMap<>();
        
        Server server = serverService.getServerById(serverId);
        if (server == null || !"online".equals(server.getStatus())) {
            return metrics;
        }
        
        String[] queries = {
            "up",
            "prometheus_build_info",
            "prometheus_config_last_reload_successful",
            "prometheus_config_last_reload_success_timestamp_seconds"
        };
        
        for (String query : queries) {
            JSONObject result = prometheusClient.query(server.getIp(), server.getPort(), query);
            if (result != null && "success".equals(result.getString("status"))) {
                metrics.put(query.replace("prometheus_", ""), result.get("data"));
            }
        }
        
        return metrics;
    }

    public Map<String, Object> getSystemMetrics(Long serverId) {
        Map<String, Object> metrics = new HashMap<>();
        
        Server server = serverService.getServerById(serverId);
        if (server == null || !"online".equals(server.getStatus())) {
            return metrics;
        }
        
        Map<String, String> systemQueries = new HashMap<>();
        systemQueries.put("cpu_usage", "avg(rate(process_cpu_seconds_total[1m])) * 100");
        systemQueries.put("memory_usage", "process_resident_memory_bytes / 1024 / 1024");
        systemQueries.put("goroutines", "go_goroutines");
        systemQueries.put("open_fds", "process_open_fds");
        systemQueries.put("max_fds", "process_max_fds");
        
        for (Map.Entry<String, String> entry : systemQueries.entrySet()) {
            JSONObject result = prometheusClient.query(server.getIp(), server.getPort(), entry.getValue());
            Double value = prometheusClient.extractValueFromResult(result, entry.getKey());
            if (value != null) {
                metrics.put(entry.getKey(), value);
            }
        }
        
        return metrics;
    }
}
