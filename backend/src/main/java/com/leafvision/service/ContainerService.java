package com.leafvision.service;

import com.alibaba.fastjson.JSONObject;
import com.leafvision.client.PrometheusClient;
import com.leafvision.entity.Container;
import com.leafvision.entity.Server;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContainerService {

    private final PrometheusClient prometheusClient;
    private final ServerService serverService;

    public ContainerService(PrometheusClient prometheusClient, ServerService serverService) {
        this.prometheusClient = prometheusClient;
        this.serverService = serverService;
    }

    public List<Container> getAllContainers() {
        List<Container> containers = new ArrayList<>();
        
        List<Server> promServers = serverService.getServersByType("prometheus-master");
        if (promServers.isEmpty()) {
            promServers = serverService.getServersByType("prometheus-node");
        }
        
        if (promServers.isEmpty()) {
            containers.add(createDemoContainer("container-1", "nginx", "nginx:latest", "running"));
            containers.add(createDemoContainer("container-2", "redis", "redis:alpine", "running"));
            containers.add(createDemoContainer("container-3", "mysql", "mysql:8.0", "exited"));
            return containers;
        }
        
        Server promServer = promServers.stream()
                .filter(s -> "online".equals(s.getStatus()))
                .findFirst()
                .orElse(null);
        
        if (promServer == null) {
            containers.add(createDemoContainer("container-1", "nginx", "nginx:latest", "running"));
            containers.add(createDemoContainer("container-2", "redis", "redis:alpine", "running"));
            containers.add(createDemoContainer("container-3", "mysql", "mysql:8.0", "exited"));
            return containers;
        }
        
        try {
            JSONObject response = prometheusClient.query(promServer.getIp(), promServer.getPort(), "container_last_seen");
            if (response != null && "success".equals(response.getString("status"))) {
                JSONObject data = response.getJSONObject("data");
                if (data != null) {
                    var results = data.getJSONArray("result");
                    if (results != null) {
                        for (int i = 0; i < results.size(); i++) {
                            JSONObject result = results.getJSONObject(i);
                            Container container = new Container();
                            JSONObject metric = result.getJSONObject("metric");
                            if (metric != null) {
                                container.setId(metric.getString("id"));
                                container.setName(metric.getString("name"));
                                container.setImage(metric.getString("image"));
                                container.setStatus("running");
                                container.setServerId(promServer.getId());
                                container.setServerName(promServer.getName());
                            }
                            containers.add(container);
                        }
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
