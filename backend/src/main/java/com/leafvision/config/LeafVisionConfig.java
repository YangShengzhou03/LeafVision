package com.leafvision.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "leafvision")
public class LeafVisionConfig {
    
    private SyncConfig sync = new SyncConfig();
    private PrometheusConfig prometheus = new PrometheusConfig();
    private AlertmanagerConfig alertmanager = new AlertmanagerConfig();
    
    public SyncConfig getSync() {
        return sync;
    }
    
    public void setSync(SyncConfig sync) {
        this.sync = sync;
    }
    
    public PrometheusConfig getPrometheus() {
        return prometheus;
    }
    
    public void setPrometheus(PrometheusConfig prometheus) {
        this.prometheus = prometheus;
    }
    
    public AlertmanagerConfig getAlertmanager() {
        return alertmanager;
    }
    
    public void setAlertmanager(AlertmanagerConfig alertmanager) {
        this.alertmanager = alertmanager;
    }
    
    public static class SyncConfig {
        private boolean enabled = true;
        private long interval = 60000;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public long getInterval() {
            return interval;
        }
        
        public void setInterval(long interval) {
            this.interval = interval;
        }
    }
    
    public static class PrometheusConfig {
        private int timeout = 10000;
        
        public int getTimeout() {
            return timeout;
        }
        
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }
    
    public static class AlertmanagerConfig {
        private int timeout = 10000;
        
        public int getTimeout() {
            return timeout;
        }
        
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }
}
