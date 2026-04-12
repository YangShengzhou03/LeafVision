package com.leafvision.entity;

import java.util.List;
import java.util.Map;

public class DashboardData {
    private List<Map<String, Object>> statsCards;
    private List<Double> cpuTrend;
    private List<Double> memoryTrend;
    private List<Map<String, Object>> networkDistribution;
    private List<Server> serverList;

    public DashboardData() {
    }

    public DashboardData(List<Map<String, Object>> statsCards, List<Double> cpuTrend, 
                         List<Double> memoryTrend, List<Map<String, Object>> networkDistribution, 
                         List<Server> serverList) {
        this.statsCards = statsCards;
        this.cpuTrend = cpuTrend;
        this.memoryTrend = memoryTrend;
        this.networkDistribution = networkDistribution;
        this.serverList = serverList;
    }

    public List<Map<String, Object>> getStatsCards() {
        return statsCards;
    }

    public void setStatsCards(List<Map<String, Object>> statsCards) {
        this.statsCards = statsCards;
    }

    public List<Double> getCpuTrend() {
        return cpuTrend;
    }

    public void setCpuTrend(List<Double> cpuTrend) {
        this.cpuTrend = cpuTrend;
    }

    public List<Double> getMemoryTrend() {
        return memoryTrend;
    }

    public void setMemoryTrend(List<Double> memoryTrend) {
        this.memoryTrend = memoryTrend;
    }

    public List<Map<String, Object>> getNetworkDistribution() {
        return networkDistribution;
    }

    public void setNetworkDistribution(List<Map<String, Object>> networkDistribution) {
        this.networkDistribution = networkDistribution;
    }

    public List<Server> getServerList() {
        return serverList;
    }

    public void setServerList(List<Server> serverList) {
        this.serverList = serverList;
    }
}
