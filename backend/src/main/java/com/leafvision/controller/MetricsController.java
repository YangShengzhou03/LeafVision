package com.leafvision.controller;

import com.leafvision.entity.Result;
import com.leafvision.service.MetricsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "*")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/query")
    public Result<Map<String, Object>> queryMetrics(
            @RequestParam String metric,
            @RequestParam Long serverId,
            @RequestParam(required = false, defaultValue = "1h") String timeRange
    ) {
        return Result.success(metricsService.queryMetrics(metric, serverId, timeRange));
    }

    @GetMapping("/query_range")
    public Result<Map<String, Object>> queryMetricsRange(
            @RequestParam String metric,
            @RequestParam Long serverId,
            @RequestParam Long start,
            @RequestParam Long end,
            @RequestParam(required = false, defaultValue = "15s") String step
    ) {
        return Result.success(metricsService.queryMetricsRange(metric, serverId, start, end, step));
    }

    @GetMapping("/available")
    public Result<List<Map<String, Object>>> getAvailableMetrics(@RequestParam Long serverId) {
        return Result.success(metricsService.getAvailableMetrics(serverId));
    }

    @GetMapping("/instant")
    public Result<Map<String, Object>> getInstantMetrics(@RequestParam Long serverId) {
        return Result.success(metricsService.getInstantMetrics(serverId));
    }

    @GetMapping("/system")
    public Result<Map<String, Object>> getSystemMetrics(@RequestParam Long serverId) {
        return Result.success(metricsService.getSystemMetrics(serverId));
    }
}
