package com.leafvision.controller;

import com.leafvision.entity.Alert;
import com.leafvision.entity.AlertRule;
import com.leafvision.entity.Result;
import com.leafvision.service.AlertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public Result<List<Alert>> getAllAlerts() {
        return Result.success(alertService.getAllAlerts());
    }

    @GetMapping("/active")
    public Result<List<Alert>> getActiveAlerts() {
        return Result.success(alertService.getActiveAlerts());
    }

    @GetMapping("/rules")
    public Result<List<AlertRule>> getAllRules() {
        return Result.success(alertService.getAllRules());
    }

    @PutMapping("/{id}/resolve")
    public Result<Void> resolveAlert(@PathVariable Long id) {
        if (alertService.resolveAlert(id)) {
            return Result.success();
        }
        return Result.error(404, "告警不存在");
    }

    @PostMapping("/rules")
    public Result<AlertRule> addRule(@RequestBody AlertRule rule) {
        return Result.success(alertService.addRule(rule));
    }

    @PutMapping("/rules/{id}")
    public Result<AlertRule> updateRule(@PathVariable Long id, @RequestBody AlertRule rule) {
        AlertRule updated = alertService.updateRule(id, rule);
        if (updated == null) {
            return Result.error(404, "规则不存在");
        }
        return Result.success(updated);
    }

    @DeleteMapping("/rules/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        if (alertService.deleteRule(id)) {
            return Result.success();
        }
        return Result.error(404, "规则不存在");
    }

    @PutMapping("/rules/{id}/toggle")
    public Result<Void> toggleRule(@PathVariable Long id) {
        if (alertService.toggleRule(id)) {
            return Result.success();
        }
        return Result.error(404, "规则不存在");
    }

    @PostMapping("/sync")
    public Result<Void> syncAlerts() {
        alertService.syncAlertsFromAlertmanager();
        alertService.syncRulesFromPrometheus();
        return Result.success();
    }
}
