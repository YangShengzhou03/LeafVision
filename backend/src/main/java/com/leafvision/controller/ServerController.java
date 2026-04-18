package com.leafvision.controller;

import com.alibaba.fastjson.JSONObject;
import com.leafvision.client.PrometheusClient;
import com.leafvision.entity.Result;
import com.leafvision.entity.Server;
import com.leafvision.service.ServerService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servers")
@CrossOrigin(origins = "*")
public class ServerController {

    private final ServerService serverService;
    private final PrometheusClient prometheusClient;

    public ServerController(ServerService serverService, PrometheusClient prometheusClient) {
        this.serverService = serverService;
        this.prometheusClient = prometheusClient;
    }

    @GetMapping
    public Result<List<Server>> getAllServers() {
        return Result.success(serverService.getAllServers());
    }

    @GetMapping("/{id}")
    public Result<Server> getServerById(@PathVariable Long id) {
        Server server = serverService.getServerById(id);
        if (server == null) {
            return Result.error(404, "服务器不存在");
        }
        return Result.success(server);
    }

    @PostMapping
    public Result<Server> addServer(@RequestBody Server server) {
        try {
            return Result.success(serverService.addServer(server));
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Server> updateServer(@PathVariable Long id, @RequestBody Server server) {
        try {
            Server updated = serverService.updateServer(id, server);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteServer(@PathVariable Long id) {
        if (serverService.deleteServer(id)) {
            return Result.success();
        }
        return Result.error(404, "服务器不存在");
    }

    @PostMapping("/refresh")
    public Result<Void> refreshStatus() {
        serverService.refreshStatus();
        return Result.success();
    }

    @GetMapping("/{id}/realtime")
    public Result<Map<String, Object>> getRealtimeMetrics(@PathVariable Long id) {
        Server server = serverService.getServerById(id);
        if (server == null) {
            return Result.error(404, "服务器不存在");
        }
        
        Map<String, Object> metrics = new HashMap<>();
        
        if ("prometheus-master".equals(server.getType()) || "prometheus-node".equals(server.getType())) {
            try {
                JSONObject cpuResult = prometheusClient.query(server.getIp(), server.getPort(), 
                    "100 - (avg by(instance) (irate(node_cpu_seconds_total{mode=\"idle\"}[1m])) * 100)");
                Double cpu = prometheusClient.extractValueFromResult(cpuResult, "cpu");
                metrics.put("cpu", cpu != null ? cpu : 0);
                
                JSONObject memResult = prometheusClient.query(server.getIp(), server.getPort(),
                    "100 - ((node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes) * 100)");
                Double memory = prometheusClient.extractValueFromResult(memResult, "memory");
                metrics.put("memory", memory != null ? memory : 0);
                
                JSONObject netInResult = prometheusClient.query(server.getIp(), server.getPort(),
                    "irate(node_network_receive_bytes_total{device!=\"lo\"}[1m])");
                Double networkIn = prometheusClient.extractValueFromResult(netInResult, "networkIn");
                metrics.put("networkIn", networkIn != null ? networkIn / 1024 : 0);
                
                JSONObject netOutResult = prometheusClient.query(server.getIp(), server.getPort(),
                    "irate(node_network_transmit_bytes_total{device!=\"lo\"}[1m])");
                Double networkOut = prometheusClient.extractValueFromResult(netOutResult, "networkOut");
                metrics.put("networkOut", networkOut != null ? networkOut / 1024 : 0);
            } catch (Exception e) {
                metrics.put("cpu", 0);
                metrics.put("memory", 0);
                metrics.put("networkIn", 0);
                metrics.put("networkOut", 0);
            }
        } else {
            metrics.put("cpu", 0);
            metrics.put("memory", 0);
            metrics.put("networkIn", 0);
            metrics.put("networkOut", 0);
        }
        
        return Result.success(metrics);
    }
}
