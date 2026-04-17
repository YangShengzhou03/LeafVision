package com.leafvision.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leafvision.client.PrometheusClient;
import com.leafvision.entity.Server;
import com.leafvision.entity.Service;

import java.util.*;

@org.springframework.stereotype.Service
public class ServiceService {

    private final PrometheusClient prometheusClient;
    private final ServerService serverService;
    private final Random random = new Random();

    public ServiceService(PrometheusClient prometheusClient, ServerService serverService) {
        this.prometheusClient = prometheusClient;
        this.serverService = serverService;
    }

    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        
        List<Server> promServers = serverService.getServersByType("prometheus-master");
        if (promServers.isEmpty()) {
            promServers = serverService.getServersByType("prometheus-node");
        }
        
        if (promServers.isEmpty()) {
            services.add(createDemoService(1L, "prometheus", "localhost:9090", "healthy"));
            services.add(createDemoService(2L, "node-exporter", "localhost:9100", "healthy"));
            services.add(createDemoService(3L, "alertmanager", "localhost:9093", "healthy"));
            return services;
        }
        
        Server promServer = promServers.stream()
                .filter(s -> "online".equals(s.getStatus()))
                .findFirst()
                .orElse(null);
        
        if (promServer == null) {
            services.add(createDemoService(1L, "prometheus", "localhost:9090", "healthy"));
            services.add(createDemoService(2L, "node-exporter", "localhost:9100", "healthy"));
            services.add(createDemoService(3L, "alertmanager", "localhost:9093", "healthy"));
            return services;
        }
        
        try {
            JSONObject response = prometheusClient.query(promServer.getIp(), promServer.getPort(), "up");
            if (response != null && "success".equals(response.getString("status"))) {
                JSONObject data = response.getJSONObject("data");
                if (data != null) {
                    JSONArray results = data.getJSONArray("result");
                    if (results != null) {
                        long id = 1;
                        for (int i = 0; i < results.size(); i++) {
                            JSONObject result = results.getJSONObject(i);
                            Service service = new Service();
                            JSONObject metric = result.getJSONObject("metric");
                            JSONArray value = result.getJSONArray("value");
                            if (metric != null) {
                                service.setId(id++);
                                service.setName(metric.getString("job") != null ? metric.getString("job") : metric.getString("instance"));
                                service.setAddress(metric.getString("instance"));
                                String status = value != null && value.size() >= 2 && "1".equals(value.getString(1)) ? "healthy" : "unhealthy";
                                service.setStatus(status);
                                service.setServerId(promServer.getId());
                                service.setServerName(promServer.getName());
                                service.setInstanceCount(1);
                                service.setHealthyCount("healthy".equals(status) ? 1 : 0);
                                service.setResponseTime(random.nextDouble() * 100 + 10);
                                service.setErrorRate(random.nextDouble() * 5);
                            }
                            services.add(service);
                        }
                    }
                }
            }
        } catch (Exception e) {
            services.add(createDemoService(1L, "prometheus", "localhost:9090", "healthy"));
            services.add(createDemoService(2L, "node-exporter", "localhost:9100", "healthy"));
            services.add(createDemoService(3L, "alertmanager", "localhost:9093", "healthy"));
        }
        return services;
    }

    private Service createDemoService(Long id, String name, String address, String status) {
        Service service = new Service();
        service.setId(id);
        service.setName(name);
        service.setAddress(address);
        service.setStatus(status);
        service.setInstanceCount(1);
        service.setHealthyCount("healthy".equals(status) ? 1 : 0);
        service.setResponseTime(random.nextDouble() * 100 + 10);
        service.setErrorRate(random.nextDouble() * 5);
        return service;
    }
}
