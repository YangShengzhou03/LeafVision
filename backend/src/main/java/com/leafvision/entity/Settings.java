package com.leafvision.entity;

import java.util.HashMap;
import java.util.Map;

public class Settings {
    private Map<String, Object> data = new HashMap<>();

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    
    public Object get(String key) { return data.get(key); }
    public void set(String key, Object value) { data.put(key, value); }
}
