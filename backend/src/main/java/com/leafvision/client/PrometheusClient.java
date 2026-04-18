package com.leafvision.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class PrometheusClient {

    private static final Logger log = LoggerFactory.getLogger(PrometheusClient.class);

    private final WebClient webClient;

    public PrometheusClient() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public boolean checkHealth(String host, Integer port) {
        try {
            String response = webClient.get()
                    .uri(String.format("http://%s:%d/-/healthy", host, port))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return response != null && response.contains("Healthy");
        } catch (Exception e) {
            log.warn("Prometheus health check failed for {}:{}", host, port, e);
            return false;
        }
    }

    public JSONObject query(String host, Integer port, String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            URI uri = URI.create(String.format("http://%s:%d/api/v1/query?query=%s", host, port, encodedQuery));
            String response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return JSON.parseObject(response);
        } catch (Exception e) {
            log.error("Prometheus query failed: {}", query, e);
            return null;
        }
    }

    public JSONObject queryRange(String host, Integer port, String query, long start, long end, String step) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            URI uri = URI.create(String.format("http://%s:%d/api/v1/query_range?query=%s&start=%d&end=%d&step=%s", 
                    host, port, encodedQuery, start, end, step));
            String response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            return JSON.parseObject(response);
        } catch (Exception e) {
            log.error("Prometheus range query failed: {}", query, e);
            return null;
        }
    }

    public JSONObject getMetrics(String host, Integer port) {
        try {
            String response = webClient.get()
                    .uri(String.format("http://%s:%d/api/v1/metadata", host, port))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return JSON.parseObject(response);
        } catch (Exception e) {
            log.error("Failed to get Prometheus metrics metadata", e);
            return null;
        }
    }

    public JSONObject getRules(String host, Integer port) {
        try {
            String response = webClient.get()
                    .uri(String.format("http://%s:%d/api/v1/rules", host, port))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return JSON.parseObject(response);
        } catch (Exception e) {
            log.error("Failed to get Prometheus rules", e);
            return null;
        }
    }

    public Double extractValueFromResult(JSONObject result, String metricName) {
        try {
            if (result == null || !"success".equals(result.getString("status"))) {
                return null;
            }
            JSONObject data = result.getJSONObject("data");
            if (data == null) {
                return null;
            }
            var results = data.getJSONArray("result");
            if (results == null || results.isEmpty()) {
                return null;
            }
            JSONObject firstResult = results.getJSONObject(0);
            var value = firstResult.getJSONArray("value");
            if (value != null && value.size() >= 2) {
                return value.getDouble(1);
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to extract value from Prometheus result", e);
            return null;
        }
    }
}
