package com.leafvision.controller;

import com.alibaba.fastjson.JSONObject;
import com.leafvision.entity.Result;
import com.leafvision.entity.Container;
import com.leafvision.service.ContainerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/containers")
@CrossOrigin(origins = "*")
public class ContainerController {

    private final ContainerService containerService;

    public ContainerController(ContainerService containerService) {
        this.containerService = containerService;
    }

    @GetMapping
    public Result<List<Container>> getAllContainers() {
        return Result.success(containerService.getAllContainers());
    }

    @GetMapping("/{id}")
    public Result<Container> getContainerDetail(@PathVariable String id, @RequestParam(required = false) String serverId) {
        Container container = containerService.getContainerDetail(id, serverId);
        if (container == null) {
            return Result.error(404, "容器不存在");
        }
        return Result.success(container);
    }

    @PostMapping("/{id}/start")
    public Result<Void> startContainer(@PathVariable String id, @RequestBody(required = false) Map<String, String> params) {
        String serverId = params != null ? params.get("serverId") : null;
        boolean success = containerService.startContainer(id, serverId);
        if (success) {
            return Result.success(null);
        }
        return Result.error(500, "启动容器失败");
    }

    @PostMapping("/{id}/stop")
    public Result<Void> stopContainer(@PathVariable String id, @RequestBody(required = false) Map<String, String> params) {
        String serverId = params != null ? params.get("serverId") : null;
        boolean success = containerService.stopContainer(id, serverId);
        if (success) {
            return Result.success(null);
        }
        return Result.error(500, "停止容器失败");
    }

    @PostMapping("/{id}/restart")
    public Result<Void> restartContainer(@PathVariable String id, @RequestBody(required = false) Map<String, String> params) {
        String serverId = params != null ? params.get("serverId") : null;
        boolean success = containerService.restartContainer(id, serverId);
        if (success) {
            return Result.success(null);
        }
        return Result.error(500, "重启容器失败");
    }

    @GetMapping("/{id}/logs")
    public Result<String> getContainerLogs(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "100") int tail,
            @RequestParam(required = false) String serverId) {
        String logs = containerService.getContainerLogs(id, serverId, tail);
        return Result.success(logs);
    }

    @GetMapping("/{id}/stats")
    public Result<JSONObject> getContainerStats(@PathVariable String id, @RequestParam(required = false) String serverId) {
        JSONObject stats = containerService.getContainerStats(id, serverId);
        return Result.success(stats);
    }

    @PostMapping
    public Result<String> createContainer(@RequestBody Map<String, Object> params) {
        String containerId = containerService.createContainer(params);
        if (containerId != null) {
            return Result.success(containerId);
        }
        return Result.error(500, "创建容器失败");
    }
}
