package com.leafvision.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SettingsService {

    private Map<String, Object> settings = new HashMap<>();

    public SettingsService() {
        settings.put("prometheusUrl", "http://localhost:9090");
        settings.put("alertmanagerUrl", "http://localhost:9093");
        settings.put("refreshInterval", 30);
        settings.put("theme", "light");
        settings.put("language", "zh-CN");
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void updateSettings(Map<String, Object> newSettings) {
        if (newSettings != null) {
            settings.putAll(newSettings);
        }
    }

    public Object getSetting(String key) {
        return settings.get(key);
    }

    public void setSetting(String key, Object value) {
        settings.put(key, value);
    }
}
