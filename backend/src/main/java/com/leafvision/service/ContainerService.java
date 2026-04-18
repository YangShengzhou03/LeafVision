package com.leafvision.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leafvision.client.DockerClient;
import com.leafvision.client.PrometheusClient;
import com.leafvision.entity.Container;
import com.leafvision.entity.Server;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContainerService {

    private final DockerClient dockerClient;
    private final PrometheusClient prometheusClient;
    private final ServerService serverService;

    public ContainerService(DockerClient dockerClient, PrometheusClient prometheusClient, ServerService serverService) {
        this.dockerClient = dockerClient;
        this.prometheusClient = prometheusClient;
        this.serverService = serverService;
    }

    public List<Container> getAllContainers() {
        List<Container> containers = new ArrayList<>();
        
        List<Server> dockerServers = serverService.getServersByType("docker");
        if (dockerServers.isEmpty()) {
            dockerServers = serverService.getServersByType("prometheus-master");
        }
        
        if (dockerServers.isEmpty()) {
            return containers;
        }
        
        for (Server server : dockerServers) {
            if (!"online".equals(server.getStatus())) continue;
            
            try {
                JSONArray dockerContainers = dockerClient.listContainers(server.getIp(), 
                        server.getDockerPort() != null ? server.getDockerPort() : 2375, true);
                
                if (dockerContainers != null) {
                    for (int i = 0; i < dockerContainers.size(); i++) {
                        JSONObject c = dockerContainers.getJSONObject(i);
                        Container container = parseDockerContainer(c, server);
                        containers.add(container);
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
        
        return containers;
    }

    private Container parseDockerContainer(JSONObject c, Server server) {
        Container container = new Container();
        container.setId(c.getString("Id").substring(0, 12));
        
        JSONArray names = c.getJSONArray("Names");
        if (names != null && !names.isEmpty()) {
            String name = names.getString(0);
            container.setName(name.startsWith("/") ? name.substring(1) : name);
        }
        
        container.setImage(c.getString("Image"));
        
        String state = c.getString("State");
        container.setStatus(state);
        container.setState(state);
        
        JSONObject stateObj = c.getJSONObject("State");
        if (stateObj != null) {
            container.setStatus(stateObj.getString("Status"));
        }
        
        container.setServerId(server.getId());
        container.setServerName(server.getName());
        
        JSONArray ports = c.getJSONArray("Ports");
        if (ports != null && !ports.isEmpty()) {
            StringBuilder portStr = new StringBuilder();
            for (int i = 0; i < ports.size(); i++) {
                JSONObject p = ports.getJSONObject(i);
                if (p.containsKey("PublicPort")) {
                    if (portStr.length() > 0) portStr.append(", ");
                    portStr.append(p.getString("PublicPort")).append("->").append(p.getString("PrivatePort"));
                }
            }
            container.setPorts(portStr.toString());
        }
        
        return container;
    }

    public Container getContainerDetail(String containerId, String serverId) {
        Server server = getDockerServer(serverId);
        if (server == null) return null;
        
        Integer dockerPort = server.getDockerPort() != null ? server.getDockerPort() : 2375;
        JSONObject detail = dockerClient.inspectContainer(server.getIp(), dockerPort, containerId);
        
        if (detail == null) return null;
        
        Container container = new Container();
        container.setId(containerId);
        container.setName(detail.getJSONObject("Name") != null ? 
                detail.getString("Name").replaceFirst("/", "") : containerId);
        
        JSONObject config = detail.getJSONObject("Config");
        if (config != null) {
            container.setImage(config.getString("Image"));
        }
        
        JSONObject state = detail.getJSONObject("State");
        if (state != null) {
            container.setStatus(state.getString("Status"));
            container.setState(state.getString("Status"));
        }
        
        return container;
    }

    public boolean startContainer(String containerId, String serverId) {
        Server server = getDockerServer(serverId);
        if (server == null) return false;
        
        Integer dockerPort = server.getDockerPort() != null ? server.getDockerPort() : 2375;
        return dockerClient.startContainer(server.getIp(), dockerPort, containerId);
    }

    public boolean stopContainer(String containerId, String serverId) {
        Server server = getDockerServer(serverId);
        if (server == null) return false;
        
        Integer dockerPort = server.getDockerPort() != null ? server.getDockerPort() : 2375;
        return dockerClient.stopContainer(server.getIp(), dockerPort, containerId);
    }

    public boolean restartContainer(String containerId, String serverId) {
        Server server = getDockerServer(serverId);
        if (server == null) return false;
        
        Integer dockerPort = server.getDockerPort() != null ? server.getDockerPort() : 2375;
        return dockerClient.restartContainer(server.getIp(), dockerPort, containerId);
    }

    public String getContainerLogs(String containerId, String serverId, int tail) {
        Server server = getDockerServer(serverId);
        if (server == null) return null;
        
        Integer dockerPort = server.getDockerPort() != null ? server.getDockerPort() : 2375;
        return dockerClient.getContainerLogs(server.getIp(), dockerPort, containerId, tail);
    }

    public JSONObject getContainerStats(String containerId, String serverId) {
        Server server = getDockerServer(serverId);
        if (server == null) return null;
        
        Integer dockerPort = server.getDockerPort() != null ? server.getDockerPort() : 2375;
        JSONObject stats = dockerClient.getContainerStats(server.getIp(), dockerPort, containerId);
        
        if (stats != null) {
            JSONObject result = new JSONObject();
            
            JSONObject cpuStats = stats.getJSONObject("cpu_stats");
            JSONObject precpuStats = stats.getJSONObject("precpu_stats");
            if (cpuStats != null && precpuStats != null) {
                JSONObject cpuUsage = cpuStats.getJSONObject("cpu_usage");
                JSONObject precpuUsage = precpuStats.getJSONObject("cpu_usage");
                if (cpuUsage != null && precpuUsage != null) {
                    long cpuDelta = cpuUsage.getLongValue("total_usage") - precpuUsage.getLongValue("total_usage");
                    long systemDelta = cpuStats.getJSONObject("system_cpu_usage").getLongValue("system_cpu_usage") - 
                                      precpuStats.getJSONObject("system_cpu_usage").getLongValue("system_cpu_usage");
                    if (systemDelta > 0) {
                        double cpuPercent = (double) cpuDelta / systemDelta * 100;
                        result.put("cpu", String.format("%.1f", cpuPercent));
                    }
                }
            }
            
            JSONObject memoryStats = stats.getJSONObject("memory_stats");
            if (memoryStats != null) {
                long usage = memoryStats.getLongValue("usage");
                long limit = memoryStats.getLongValue("limit");
                if (limit > 0) {
                    double memoryPercent = (double) usage / limit * 100;
                    result.put("memory", String.format("%.1f", memoryPercent));
                    result.put("memoryUsage", formatBytes(usage));
                    result.put("memoryLimit", formatBytes(limit));
                }
            }
            
            JSONObject networks = stats.getJSONObject("networks");
            if (networks != null) {
                long rxBytes = 0, txBytes = 0;
                for (String key : networks.keySet()) {
                    JSONObject net = networks.getJSONObject(key);
                    rxBytes += net.getLongValue("rx_bytes");
                    txBytes += net.getLongValue("tx_bytes");
                }
                result.put("networkRx", formatBytes(rxBytes));
                result.put("networkTx", formatBytes(txBytes));
            }
            
            return result;
        }
        
        return null;
    }

    private Server getDockerServer(String serverId) {
        if (serverId != null && !serverId.isEmpty() && !"null".equals(serverId)) {
            try {
                return serverService.getServerById(Long.parseLong(serverId));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        List<Server> dockerServers = serverService.getServersByType("docker");
        if (!dockerServers.isEmpty()) {
            return dockerServers.stream()
                    .filter(s -> "online".equals(s.getStatus()))
                    .findFirst()
                    .orElse(dockerServers.get(0));
        }
        List<Server> promServers = serverService.getServersByType("prometheus-master");
        if (!promServers.isEmpty()) {
            return promServers.stream()
                    .filter(s -> "online".equals(s.getStatus()))
                    .findFirst()
                    .orElse(promServers.get(0));
        }
        return null;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    public String createContainer(Map<String, Object> params) {
        Server server = getDockerServer(null);
        if (server == null) {
            return null;
        }
        
        Integer dockerPort = server.getDockerPort() != null ? server.getDockerPort() : 2375;
        
        String name = (String) params.get("name");
        String image = (String) params.get("image");
        String ports = (String) params.get("ports");
        String env = (String) params.get("env");
        
        JSONObject config = new JSONObject();
        JSONObject hostConfig = new JSONObject();
        
        config.put("Image", image);
        
        if (env != null && !env.isEmpty()) {
            String[] envLines = env.split("\n");
            JSONArray envArray = new JSONArray();
            for (String line : envLines) {
                if (!line.trim().isEmpty()) {
                    envArray.add(line.trim());
                }
            }
            config.put("Env", envArray);
        }
        
        if (ports != null && !ports.isEmpty()) {
            JSONArray portBindings = new JSONArray();
            JSONArray exposedPorts = new JSONArray();
            
            String[] portMappings = ports.split(",");
            for (String mapping : portMappings) {
                mapping = mapping.trim();
                String[] parts = mapping.split(":");
                if (parts.length == 2) {
                    String hostPort = parts[0];
                    String containerPort = parts[1];
                    
                    JSONObject binding = new JSONObject();
                    binding.put("HostPort", hostPort);
                    
                    JSONObject portBinding = new JSONObject();
                    portBinding.put("HostIp", "");
                    portBinding.put("HostPort", hostPort);
                    
                    String containerPortKey = containerPort + "/tcp";
                    exposedPorts.add(containerPortKey);
                    
                    JSONArray bindings = new JSONArray();
                    bindings.add(portBinding);
                    portBindings.add(bindings);
                }
            }
            
            config.put("ExposedPorts", exposedPorts);
            hostConfig.put("PortBindings", portBindings);
        }
        
        config.put("HostConfig", hostConfig);
        
        JSONObject result = dockerClient.createContainer(server.getIp(), dockerPort, name, config);
        
        if (result != null && result.containsKey("Id")) {
            return result.getString("Id").substring(0, 12);
        }
        
        return null;
    }
}
