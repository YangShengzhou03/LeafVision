package com.leafvision.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leafvision.client.AlertmanagerClient;
import com.leafvision.client.PrometheusClient;
import com.leafvision.entity.Alert;
import com.leafvision.entity.AlertRule;
import com.leafvision.entity.Server;
import com.leafvision.mapper.AlertMapper;
import com.leafvision.mapper.AlertRuleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertMapper alertMapper;
    private final AlertRuleMapper alertRuleMapper;
    private final AlertmanagerClient alertmanagerClient;
    private final PrometheusClient prometheusClient;
    private final ServerService serverService;

    public AlertService(AlertMapper alertMapper,
                        AlertRuleMapper alertRuleMapper,
                        AlertmanagerClient alertmanagerClient,
                        PrometheusClient prometheusClient,
                        ServerService serverService) {
        this.alertMapper = alertMapper;
        this.alertRuleMapper = alertRuleMapper;
        this.alertmanagerClient = alertmanagerClient;
        this.prometheusClient = prometheusClient;
        this.serverService = serverService;
    }

    public List<Alert> getAllAlerts() {
        return alertMapper.selectList(null);
    }

    public List<Alert> getActiveAlerts() {
        QueryWrapper<Alert> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "firing");
        return alertMapper.selectList(wrapper);
    }

    public List<AlertRule> getAllRules() {
        return alertRuleMapper.selectList(null);
    }

    @Transactional
    public boolean resolveAlert(Long id) {
        Alert alert = alertMapper.selectById(id);
        if (alert != null) {
            alert.setStatus("resolved");
            alert.setEndsAt(LocalDateTime.now());
            alert.setUpdatedAt(LocalDateTime.now());
            alertMapper.updateById(alert);
            return true;
        }
        return false;
    }

    @Transactional
    public AlertRule addRule(AlertRule rule) {
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        if (rule.getEnabled() == null) {
            rule.setEnabled(true);
        }
        alertRuleMapper.insert(rule);
        return rule;
    }

    @Transactional
    public AlertRule updateRule(Long id, AlertRule rule) {
        AlertRule existing = alertRuleMapper.selectById(id);
        if (existing == null) {
            return null;
        }
        
        existing.setName(rule.getName());
        existing.setExpr(rule.getExpr());
        existing.setDuration(rule.getDuration());
        existing.setSeverity(rule.getSeverity());
        existing.setSummary(rule.getSummary());
        existing.setDescription(rule.getDescription());
        existing.setUpdatedAt(LocalDateTime.now());
        
        alertRuleMapper.updateById(existing);
        return existing;
    }

    @Transactional
    public boolean deleteRule(Long id) {
        return alertRuleMapper.deleteById(id) > 0;
    }

    @Transactional
    public boolean toggleRule(Long id) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule != null) {
            rule.setEnabled(!rule.getEnabled());
            rule.setUpdatedAt(LocalDateTime.now());
            alertRuleMapper.updateById(rule);
            return true;
        }
        return false;
    }

    @Transactional
    public void syncAlertsFromAlertmanager() {
        List<Server> alertmanagers = serverService.getServersByType("alertmanager");
        
        for (Server am : alertmanagers) {
            if (!"online".equals(am.getStatus())) {
                continue;
            }
            
            try {
                JSONObject result = alertmanagerClient.getAlerts(am.getIp(), am.getPort(), true, true, true);
                if (result != null && "success".equals(result.getString("status"))) {
                    JSONArray alerts = result.getJSONArray("data");
                    if (alerts != null) {
                        for (int i = 0; i < alerts.size(); i++) {
                            JSONObject alertJson = alerts.getJSONObject(i);
                            processAlert(alertJson, am.getId());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to sync alerts from Alertmanager: {}", am.getName(), e);
            }
        }
    }

    private void processAlert(JSONObject alertJson, Long serverId) {
        JSONObject annotations = alertJson.getJSONObject("annotations");
        JSONObject labels = alertJson.getJSONObject("labels");
        JSONObject status = alertJson.getJSONObject("status");
        
        String fingerprint = alertJson.getString("fingerprint");
        
        Optional<Alert> existingOpt = alertMapper.findByFingerprint(fingerprint);
        Alert alert;
        
        if (existingOpt.isPresent()) {
            alert = existingOpt.get();
        } else {
            alert = new Alert();
            alert.setFingerprint(fingerprint);
            alert.setServerId(serverId);
        }
        
        if (labels != null) {
            alert.setName(labels.getString("alertname"));
            alert.setSeverity(labels.getString("severity"));
            alert.setInstance(labels.getString("instance"));
            
            Map<String, String> labelMap = new HashMap<>();
            for (String key : labels.keySet()) {
                labelMap.put(key, labels.getString(key));
            }
            alert.setLabels(labelMap);
        }
        
        if (annotations != null) {
            alert.setSummary(annotations.getString("summary"));
            alert.setDescription(annotations.getString("description"));
        }
        
        if (status != null) {
            JSONArray state = status.getJSONArray("state");
            if (state != null && !state.isEmpty()) {
                alert.setStatus(state.getString(0));
            }
        }
        
        Long startsAt = alertJson.getLong("startsAt");
        if (startsAt != null) {
            alert.setStartsAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(startsAt), ZoneId.systemDefault()));
        }
        
        Long endsAt = alertJson.getLong("endsAt");
        if (endsAt != null && endsAt > 0) {
            alert.setEndsAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(endsAt), ZoneId.systemDefault()));
        }
        
        alert.setUpdatedAt(LocalDateTime.now());
        alert.setSource("alertmanager");
        
        if (existingOpt.isPresent()) {
            alertMapper.updateById(alert);
        } else {
            alertMapper.insert(alert);
        }
    }

    @Transactional
    public void syncRulesFromPrometheus() {
        List<Server> promServers = serverService.getServersByType("prometheus-master");
        promServers.addAll(serverService.getServersByType("prometheus-node"));
        
        for (Server prom : promServers) {
            if (!"online".equals(prom.getStatus())) {
                continue;
            }
            
            try {
                JSONObject result = prometheusClient.getRules(prom.getIp(), prom.getPort());
                if (result != null && "success".equals(result.getString("status"))) {
                    JSONObject data = result.getJSONObject("data");
                    if (data != null) {
                        JSONArray groups = data.getJSONArray("groups");
                        if (groups != null) {
                            for (int i = 0; i < groups.size(); i++) {
                                JSONObject group = groups.getJSONObject(i);
                                String groupName = group.getString("name");
                                JSONArray rules = group.getJSONArray("rules");
                                if (rules != null) {
                                    for (int j = 0; j < rules.size(); j++) {
                                        JSONObject ruleJson = rules.getJSONObject(j);
                                        processRule(ruleJson, prom.getId(), groupName);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to sync rules from Prometheus: {}", prom.getName(), e);
            }
        }
    }

    private void processRule(JSONObject ruleJson, Long serverId, String groupName) {
        String name = ruleJson.getString("name");
        
        QueryWrapper<AlertRule> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name).eq("server_id", serverId);
        AlertRule existing = alertRuleMapper.selectOne(wrapper);
        
        AlertRule rule;
        if (existing != null) {
            rule = existing;
        } else {
            rule = new AlertRule();
            rule.setName(name);
            rule.setServerId(serverId);
            rule.setGroupName(groupName);
            rule.setCreatedAt(LocalDateTime.now());
        }
        
        rule.setExpr(ruleJson.getString("query"));
        rule.setDuration(ruleJson.getString("duration"));
        rule.setSeverity(ruleJson.getString("severity"));
        rule.setSource("prometheus");
        rule.setUpdatedAt(LocalDateTime.now());
        
        JSONObject labels = ruleJson.getJSONObject("labels");
        if (labels != null) {
            rule.setSeverity(labels.getString("severity"));
        }
        
        JSONObject annotations = ruleJson.getJSONObject("annotations");
        if (annotations != null) {
            rule.setSummary(annotations.getString("summary"));
            rule.setDescription(annotations.getString("description"));
        }
        
        String state = ruleJson.getString("state");
        rule.setEnabled("inactive".equals(state) ? false : true);
        
        if (existing != null) {
            alertRuleMapper.updateById(rule);
        } else {
            alertRuleMapper.insert(rule);
        }
    }

    @Transactional
    public void processWebhookAlert(JSONObject webhookData) {
        log.info("Processing webhook alert data");
        
        JSONArray alerts = webhookData.getJSONArray("alerts");
        if (alerts == null || alerts.isEmpty()) {
            log.warn("No alerts in webhook data");
            return;
        }
        
        for (int i = 0; i < alerts.size(); i++) {
            JSONObject alertJson = alerts.getJSONObject(i);
            processWebhookAlertItem(alertJson);
        }
    }

    private void processWebhookAlertItem(JSONObject alertJson) {
        String status = alertJson.getString("status");
        JSONObject labels = alertJson.getJSONObject("labels");
        JSONObject annotations = alertJson.getJSONObject("annotations");
        
        String fingerprint = generateFingerprint(labels);
        
        Optional<Alert> existingOpt = alertMapper.findByFingerprint(fingerprint);
        Alert alert;
        
        if (existingOpt.isPresent()) {
            alert = existingOpt.get();
        } else {
            alert = new Alert();
            alert.setFingerprint(fingerprint);
        }
        
        if (labels != null) {
            alert.setName(labels.getString("alertname"));
            alert.setSeverity(labels.getString("severity"));
            alert.setInstance(labels.getString("instance"));
            
            Map<String, String> labelMap = new HashMap<>();
            for (String key : labels.keySet()) {
                labelMap.put(key, labels.getString(key));
            }
            alert.setLabels(labelMap);
        }
        
        if (annotations != null) {
            alert.setSummary(annotations.getString("summary"));
            alert.setDescription(annotations.getString("description"));
        }
        
        alert.setStatus(status);
        
        String startsAtStr = alertJson.getString("startsAt");
        if (startsAtStr != null && !startsAtStr.isEmpty()) {
            try {
                alert.setStartsAt(LocalDateTime.parse(startsAtStr.replace("Z", "")).atZone(ZoneId.systemDefault()).toLocalDateTime());
            } catch (Exception e) {
                log.warn("Failed to parse startsAt: {}", startsAtStr);
            }
        }
        
        String endsAtStr = alertJson.getString("endsAt");
        if (endsAtStr != null && !endsAtStr.isEmpty() && !"0001-01-01T00:00:00Z".equals(endsAtStr)) {
            try {
                alert.setEndsAt(LocalDateTime.parse(endsAtStr.replace("Z", "")).atZone(ZoneId.systemDefault()).toLocalDateTime());
            } catch (Exception e) {
                log.warn("Failed to parse endsAt: {}", endsAtStr);
            }
        }
        
        alert.setUpdatedAt(LocalDateTime.now());
        alert.setSource("webhook");
        
        if (existingOpt.isPresent()) {
            alertMapper.updateById(alert);
        } else {
            alertMapper.insert(alert);
        }
        
        log.info("Processed webhook alert: {} - {}", alert.getName(), alert.getStatus());
    }

    private String generateFingerprint(JSONObject labels) {
        if (labels == null) {
            return UUID.randomUUID().toString();
        }
        StringBuilder sb = new StringBuilder();
        labels.keySet().stream().sorted().forEach(key -> {
            sb.append(key).append("=").append(labels.getString(key)).append(";");
        });
        return Integer.toHexString(sb.toString().hashCode());
    }
}
