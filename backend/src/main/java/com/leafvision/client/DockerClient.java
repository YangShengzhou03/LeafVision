package com.leafvision.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class DockerClient {

    private static final Logger log = LoggerFactory.getLogger(DockerClient.class);

    private final WebClient webClient;

    public DockerClient() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
                .build();
    }

    public boolean checkHealth(String host, Integer port) {
        try {
            String response = webClient.get()
                    .uri(String.format("http://%s:%d/_ping", host, port))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return response != null && response.contains("OK");
        } catch (Exception e) {
            log.warn("Docker health check failed for {}:{}", host, port, e);
            return false;
        }
    }

    public JSONArray listContainers(String host, Integer port, boolean all) {
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host(host)
                            .port(port)
                            .path("/containers/json")
                            .queryParam("all", all)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return JSON.parseArray(response);
        } catch (Exception e) {
            log.error("Failed to list containers from {}:{}", host, port, e);
            return null;
        }
    }

    public JSONObject inspectContainer(String host, Integer port, String containerId) {
        try {
            String response = webClient.get()
                    .uri(String.format("http://%s:%d/containers/%s/json", host, port, containerId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return JSON.parseObject(response);
        } catch (Exception e) {
            log.error("Failed to inspect container {}", containerId, e);
            return null;
        }
    }

    public boolean startContainer(String host, Integer port, String containerId) {
        try {
            webClient.post()
                    .uri(String.format("http://%s:%d/containers/%s/start", host, port, containerId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            return true;
        } catch (Exception e) {
            log.error("Failed to start container {}", containerId, e);
            return false;
        }
    }

    public boolean stopContainer(String host, Integer port, String containerId) {
        try {
            webClient.post()
                    .uri(String.format("http://%s:%d/containers/%s/stop", host, port, containerId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            return true;
        } catch (Exception e) {
            log.error("Failed to stop container {}", containerId, e);
            return false;
        }
    }

    public boolean restartContainer(String host, Integer port, String containerId) {
        try {
            webClient.post()
                    .uri(String.format("http://%s:%d/containers/%s/restart", host, port, containerId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            return true;
        } catch (Exception e) {
            log.error("Failed to restart container {}", containerId, e);
            return false;
        }
    }

    public String getContainerLogs(String host, Integer port, String containerId, int tail) {
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host(host)
                            .port(port)
                            .path(String.format("/containers/%s/logs", containerId))
                            .queryParam("stdout", true)
                            .queryParam("stderr", true)
                            .queryParam("tail", tail)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return response;
        } catch (Exception e) {
            log.error("Failed to get container logs {}", containerId, e);
            return null;
        }
    }

    public JSONObject getContainerStats(String host, Integer port, String containerId) {
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host(host)
                            .port(port)
                            .path(String.format("/containers/%s/stats", containerId))
                            .queryParam("stream", false)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return JSON.parseObject(response);
        } catch (Exception e) {
            log.error("Failed to get container stats {}", containerId, e);
            return null;
        }
    }

    public JSONObject createContainer(String host, Integer port, String name, JSONObject config) {
        try {
            String response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host(host)
                            .port(port)
                            .path("/containers/create")
                            .queryParam("name", name)
                            .build())
                    .bodyValue(config)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            return JSON.parseObject(response);
        } catch (Exception e) {
            log.error("Failed to create container", e);
            return null;
        }
    }

    public boolean deleteContainer(String host, Integer port, String containerId, boolean force) {
        try {
            webClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host(host)
                            .port(port)
                            .path(String.format("/containers/%s", containerId))
                            .queryParam("force", force)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            return true;
        } catch (Exception e) {
            log.error("Failed to delete container {}", containerId, e);
            return false;
        }
    }

    public List<String> parseLogOutput(String logOutput) {
        List<String> lines = new ArrayList<>();
        if (logOutput == null || logOutput.isEmpty()) {
            return lines;
        }
        
        int i = 0;
        while (i < logOutput.length()) {
            if (i + 8 > logOutput.length()) break;
            
            i += 4;
            
            int length = ((logOutput.charAt(i) & 0xFF) << 24) |
                        ((logOutput.charAt(i + 1) & 0xFF) << 16) |
                        ((logOutput.charAt(i + 2) & 0xFF) << 8) |
                        (logOutput.charAt(i + 3) & 0xFF);
            i += 4;
            
            if (i + length > logOutput.length()) break;
            
            String line = logOutput.substring(i, i + length);
            lines.add(line);
            i += length;
        }
        
        return lines;
    }
}
