package com.leafvision.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leafvision.client.PrometheusClient;
import com.leafvision.entity.Server;
import com.leafvision.entity.Service;
import com.leafvision.mapper.ServerMapper;

import java.util.*;

@org.springframework.stereotype.Service
public class ServiceService {

    private final PrometheusClient prometheusClient;
    private final ServerService serverService;
    private final ServerMapper serverMapper;
    
    private final Map<Long, Service> customServices = new HashMap<>();
    private long idCounter = 1;

    public ServiceService(PrometheusClient prometheusClient, ServerService serverService, ServerMapper serverMapper) {
        this.prometheusClient = prometheusClient;
        this.serverService = serverService;
        this.serverMapper = serverMapper;
    }

    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        
        List<Server> promServers = serverService.getServersByType("prometheus-master");
        if (promServers.isEmpty()) {
            promServers = serverService.getServersByType("prometheus-node");
        }
        
        if (!promServers.isEmpty()) {
            Server promServer = promServers.stream()
                    .filter(s -> "online".equals(s.getStatus()))
                    .findFirst()
                    .orElse(null);
            
            if (promServer != null) {
                try {
                    JSONObject response = prometheusClient.query(promServer.getIp(), promServer.getPort(), "up");
                    if (response != null && "success".equals(response.getString("status"))) {
                        JSONObject data = response.getJSONObject("data");
                        if (data != null) {
                            JSONArray results = data.getJSONArray("result");
                            if (results != null) {
                                for (int i = 0; i < results.size(); i++) {
                                    JSONObject result = results.getJSONObject(i);
                                    Service service = parsePrometheusService(result, promServer, idCounter++);
                                    services.add(service);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        
        services.addAll(customServices.values());
        
        return services;
    }

    private Service parsePrometheusService(JSONObject result, Server server, long id) {
        Service service = new Service();
        JSONObject metric = result.getJSONObject("metric");
        JSONArray value = result.getJSONArray("value");
        
        if (metric != null) {
            service.setId(id);
            service.setName(metric.getString("job") != null ? metric.getString("job") : metric.getString("instance"));
            service.setAddress(metric.getString("instance"));
            service.setType(metric.getString("job") != null ? metric.getString("job") : "unknown");
            
            String status = value != null && value.size() >= 2 && "1".equals(value.getString(1)) ? "healthy" : "unhealthy";
            service.setStatus(status);
            service.setServerId(server.getId());
            service.setServerName(server.getName());
            service.setInstanceCount(1);
            service.setHealthyCount("healthy".equals(status) ? 1 : 0);
        }
        
        return service;
    }

    public Service getServiceById(Long id) {
        if (customServices.containsKey(id)) {
            return customServices.get(id);
        }
        
        for (Service service : getAllServices()) {
            if (service.getId().equals(id)) {
                return service;
            }
        }
        return null;
    }

    public Service createService(Service service) {
        long id = System.currentTimeMillis();
        service.setId(id);
        service.setStatus("unknown");
        service.setInstanceCount(1);
        service.setHealthyCount(0);
        customServices.put(id, service);
        return service;
    }

    public Service updateService(Long id, Service service) {
        if (customServices.containsKey(id)) {
            service.setId(id);
            customServices.put(id, service);
            return service;
        }
        return null;
    }

    public boolean deleteService(Long id) {
        return customServices.remove(id) != null;
    }

    public JSONObject getServiceMetrics(Long id) {
        Service service = getServiceById(id);
        if (service == null) return null;
        
        JSONObject metrics = new JSONObject();
        metrics.put("responseTime", 0);
        metrics.put("errorRate", 0);
        metrics.put("throughput", 0);
        metrics.put("latency", 0);
        
        return metrics;
    }
}
