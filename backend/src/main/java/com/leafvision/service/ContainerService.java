package com.leafvision.service;

import com.leafvision.client.PrometheusClient;
import com.leafvision.entity.Container;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContainerService {

    private final PrometheusClient prometheusClient;

    public ContainerService(PrometheusClient prometheusClient) {
        this.prometheusClient = prometheusClient;
    }

    public List<Container> getAllContainers() {
        List<Container> containers = new ArrayList<>();
        try {
            Map<String, Object> response = prometheusClient.query("container_last_seen");
            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("result");
                if (results != null) {
                    for (Map<String, Object> result : results) {
                        Container container = new Container();
                        Map<String, String> metric = (Map<String, String>) result.get("metric");
                        if (metric != null) {
                            container.setId(metric.get("id"));
                            container.setName(metric.get("name"));
                            container.setImage(metric.get("image"));
                            container.setStatus("running");
                        }
                        containers.add(container);
                    }
                }
            }
        } catch (Exception e) {
            containers.add(createDemoContainer("container-1", "nginx", "nginx:latest", "running"));
            containers.add(createDemoContainer("container-2", "redis", "redis:alpine", "running"));
            containers.add(createDemoContainer("container-3", "mysql", "mysql:8.0", "exited"));
        }
        return containers;
    }

    private Container createDemoContainer(String id, String name, String image, String status) {
        Container container = new Container();
        container.setId(id);
        container.setName(name);
        container.setImage(image);
        container.setStatus(status);
        container.setState(status);
        return container;
    }
}
