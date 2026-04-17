package com.leafvision.service;

import com.alibaba.fastjson.JSON;
import com.leafvision.entity.Settings;
import com.leafvision.mapper.SettingsMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SettingsService {

    private final SettingsMapper settingsMapper;

    public SettingsService(SettingsMapper settingsMapper) {
        this.settingsMapper = settingsMapper;
        initDefaultSettings();
    }

    private void initDefaultSettings() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("prometheusUrl", "http://localhost:9090");
        defaults.put("alertmanagerUrl", "http://localhost:9093");
        defaults.put("refreshInterval", "30");
        defaults.put("theme", "light");
        defaults.put("language", "zh-CN");
        defaults.put("siteName", "LeafVision");
        defaults.put("siteDescription", "基础设施可观测性平台");
        defaults.put("sessionTimeout", "30");
        defaults.put("maxLoginAttempts", "5");
        defaults.put("dataRetentionDays", "30");
        defaults.put("metricsInterval", "15");
        defaults.put("logLevel", "info");
        
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            Settings existing = settingsMapper.findByKey(entry.getKey());
            if (existing == null) {
                Settings setting = new Settings();
                setting.setSettingKey(entry.getKey());
                setting.setSettingValue(String.valueOf(entry.getValue()));
                setting.setSettingType("string");
                settingsMapper.insertOrUpdate(setting);
            }
        }
    }

    public Map<String, Object> getSettings() {
        Map<String, Object> result = new HashMap<>();
        List<Settings> settings = settingsMapper.findAll();
        for (Settings setting : settings) {
            result.put(setting.getSettingKey(), parseValue(setting.getSettingValue()));
        }
        return result;
    }

    public void updateSettings(Map<String, Object> newSettings) {
        if (newSettings == null) {
            return;
        }
        
        for (Map.Entry<String, Object> entry : newSettings.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            Settings setting = new Settings();
            setting.setSettingKey(key);
            setting.setSettingValue(JSON.toJSONString(value));
            setting.setSettingType(getType(value));
            
            settingsMapper.insertOrUpdate(setting);
        }
    }

    public Object getSetting(String key) {
        Settings setting = settingsMapper.findByKey(key);
        if (setting != null) {
            return parseValue(setting.getSettingValue());
        }
        return null;
    }

    public void setSetting(String key, Object value) {
        Settings setting = new Settings();
        setting.setSettingKey(key);
        setting.setSettingValue(JSON.toJSONString(value));
        setting.setSettingType(getType(value));
        settingsMapper.insertOrUpdate(setting);
    }

    private Object parseValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return JSON.parse(value);
        } catch (Exception e) {
            return value;
        }
    }

    private String getType(Object value) {
        if (value == null) {
            return "string";
        }
        if (value instanceof Number) {
            return "number";
        }
        if (value instanceof Boolean) {
            return "boolean";
        }
        if (value instanceof Map || value instanceof List) {
            return "json";
        }
        return "string";
    }
}
