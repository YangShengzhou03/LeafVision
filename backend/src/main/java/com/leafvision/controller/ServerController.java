package com.leafvision.controller;

import com.leafvision.entity.Result;
import com.leafvision.entity.Server;
import com.leafvision.service.ServerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
@CrossOrigin(origins = "*")
public class ServerController {

    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @GetMapping
    public Result<List<Server>> getAllServers() {
        return Result.success(serverService.getAllServers());
    }

    @GetMapping("/{id}")
    public Result<Server> getServerById(@PathVariable Long id) {
        Server server = serverService.getServerById(id);
        if (server == null) {
            return Result.error(404, "服务器不存在");
        }
        return Result.success(server);
    }

    @PostMapping
    public Result<Server> addServer(@RequestBody Server server) {
        try {
            return Result.success(serverService.addServer(server));
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Server> updateServer(@PathVariable Long id, @RequestBody Server server) {
        try {
            Server updated = serverService.updateServer(id, server);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteServer(@PathVariable Long id) {
        if (serverService.deleteServer(id)) {
            return Result.success();
        }
        return Result.error(404, "服务器不存在");
    }

    @PostMapping("/refresh")
    public Result<Void> refreshStatus() {
        serverService.refreshStatus();
        return Result.success();
    }
}
