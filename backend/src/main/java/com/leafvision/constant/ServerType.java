package com.leafvision.constant;

public final class ServerType {
    
    public static final String PROMETHEUS_MASTER = "prometheus-master";
    public static final String PROMETHEUS_NODE = "prometheus-node";
    public static final String ALERTMANAGER = "alertmanager";
    
    private ServerType() {
    }
    
    public static boolean isPrometheus(String type) {
        return PROMETHEUS_MASTER.equals(type) || PROMETHEUS_NODE.equals(type);
    }
    
    public static boolean isAlertmanager(String type) {
        return ALERTMANAGER.equals(type);
    }
    
    public static boolean isValid(String type) {
        return isPrometheus(type) || isAlertmanager(type);
    }
}
