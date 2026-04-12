package com.leafvision.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
public class AlertmanagerClient {

    private static final Logger log = LoggerFactory.getLogger(AlertmanagerClient.class);

    private final WebClient webClient;

    public AlertmanagerClient() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public boolean checkHealth(String host, Integer port) {
        String url = String.format("http://%s:%d/-/healthy", host, port);
        try {
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return response != null && response.contains("OK");
        } catch (Exception e) {
            log.warn("Alertmanager health check failed for {}:{}", host, port, e);
            return false;
        }
    }

    public JSONObject getAlerts(String host, Integer port, boolean active, boolean silenced, boolean inhibited) {
        String url = String.format("http://%s:%d/api/v1/alerts", host, port);
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("active", active)
                            .queryParam("silenced", silenced)
                            .queryParam("inhibited", inhibited)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return JSON.parseObject(response);
        } catch (Exception e) {
            log.error("Failed to get alerts from Alertmanager", e);
            return null;
        }
    }

    public JSONObject getStatus(String host, Integer port) {
        String url = String.format("http://%s:%d/api/v1/status", host, port);
        try {
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return JSON.parseObject(response);
        } catch (Exception e) {
            log.error("Failed to get Alertmanager status", e);
            return null;
        }
    }

    public JSONObject getSilences(String host, Integer port) {
        String url = String.format("http://%s:%d/api/v1/silences", host, port);
        try {
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return JSON.parseObject(response);
        } catch (Exception e) {
            log.error("Failed to get silences from Alertmanager", e);
            return null;
        }
    }

    public boolean createSilence(String host, Integer port, JSONObject silence) {
        String url = String.format("http://%s:%d/api/v1/silences", host, port);
        try {
            String response = webClient.post()
                    .uri(url)
                    .bodyValue(silence.toJSONString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return response != null;
        } catch (Exception e) {
            log.error("Failed to create silence in Alertmanager", e);
            return false;
        }
    }

    public boolean deleteSilence(String host, Integer port, String silenceId) {
        String url = String.format("http://%s:%d/api/v1/silence/%s", host, port, silenceId);
        try {
            webClient.delete()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return true;
        } catch (Exception e) {
            log.error("Failed to delete silence from Alertmanager", e);
            return false;
        }
    }
}
