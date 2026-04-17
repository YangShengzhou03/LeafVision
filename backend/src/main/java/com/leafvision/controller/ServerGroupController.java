package com.leafvision.controller;

import com.leafvision.entity.Result;
import com.leafvision.entity.ServerGroup;
import com.leafvision.service.ServerGroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/server-groups")
@CrossOrigin(origins = "*")
public class ServerGroupController {

    private final ServerGroupService groupService;

    public ServerGroupController(ServerGroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public Result<List<ServerGroup>> getAllGroups() {
        return Result.success(groupService.getAllGroups());
    }

    @GetMapping("/tree")
    public Result<List<ServerGroup>> getGroupTree() {
        return Result.success(groupService.getGroupTree());
    }

    @GetMapping("/{id}")
    public Result<ServerGroup> getGroupById(@PathVariable Long id) {
        ServerGroup group = groupService.getGroupById(id);
        if (group == null) {
            return Result.error(404, "分组不存在");
        }
        return Result.success(group);
    }

    @PostMapping
    public Result<ServerGroup> createGroup(@RequestBody ServerGroup group) {
        try {
            return Result.success(groupService.createGroup(group));
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<ServerGroup> updateGroup(@PathVariable Long id, @RequestBody ServerGroup group) {
        group.setId(id);
        try {
            return Result.success(groupService.updateGroup(group));
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteGroup(@PathVariable Long id) {
        if (groupService.deleteGroup(id)) {
            return Result.success();
        }
        return Result.error(404, "分组不存在");
    }

    @PostMapping("/{groupId}/servers/{serverId}")
    public Result<Void> addServerToGroup(@PathVariable Long groupId, @PathVariable Long serverId) {
        try {
            groupService.addServerToGroup(serverId, groupId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{groupId}/servers/{serverId}")
    public Result<Void> removeServerFromGroup(@PathVariable Long groupId, @PathVariable Long serverId) {
        try {
            groupService.removeServerFromGroup(serverId, groupId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/{id}/servers")
    public Result<?> getServersByGroup(@PathVariable Long id) {
        return Result.success(groupService.getServersByGroup(id));
    }
}
