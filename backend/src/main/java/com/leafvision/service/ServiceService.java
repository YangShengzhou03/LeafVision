package com.leafvision.service;

import com.leafvision.client.PrometheusClient;
import com.leafvision.entity.Service;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ServiceService {

    private final PrometheusClient prometheusClient;

    public ServiceService(PrometheusClient prometheusClient) {
        this.prometheusClient = prometheusClient;
    }

    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        try {
            Map<String, Object> response = prometheusClient.query("up");
            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("result");
                if (results != null) {
                    long id = 1;
                    for (Map<String, Object> result : results) {
                        Service service = new Service();
                        Map<String, String> metric = (Map<String, String>) result.get("metric");
                        Object value = result.get("value");
                        if (metric != null) {
                            service.setId(id++);
                            service.setName(metric.get("job") != null ? metric.get("job") : metric.get("instance"));
                            service.setAddress(metric.get("instance"));
                            service.setStatus(value != null && value.toString().contains("1") ? "up" : "down");
                        }
                        services.add(service);
                    }
                }
            }
        } catch (Exception e) {
            services.add(createDemoService(1L, "prometheus", "localhost:9090", "up"));
            services.add(createDemoService(2L, "node-exporter", "localhost:9100", "up"));
            services.add(createDemoService(3L, "alertmanager", "localhost:9093", "up"));
        }
        return services;
    }

    private Service createDemoService(Long id, String name, String address, String status) {
        Service service = new Service();
        service.setId(id);
        service.setName(name);
        service.setAddress(address);
        service.setStatus(status);
        return service;
    }
}
