package com.leafvision.controller;

import com.leafvision.entity.Result;
import com.leafvision.entity.Role;
import com.leafvision.service.RoleService;
import com.leafvision.service.AuditLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {

    private final RoleService roleService;
    private final AuditLogService auditLogService;

    public RoleController(RoleService roleService, AuditLogService auditLogService) {
        this.roleService = roleService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public Result<List<Role>> getAllRoles() {
        return Result.success(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public Result<Role> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }
        return Result.success(role);
    }

    @PostMapping
    public Result<Role> createRole(@RequestBody Role role) {
        try {
            Role created = roleService.createRole(role);
            auditLogService.logSuccess(null, "admin", "CREATE", "role", role.getRoleName());
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        try {
            role.setId(id);
            Role updated = roleService.updateRole(role);
            auditLogService.logSuccess(null, "admin", "UPDATE", "role", role.getRoleName());
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }
        if ("ADMIN".equals(role.getRoleCode())) {
            return Result.error(400, "系统管理员角色不可删除");
        }
        if (roleService.deleteRole(id)) {
            auditLogService.logSuccess(null, "admin", "DELETE", "role", role.getRoleName());
            return Result.success();
        }
        return Result.error(400, "删除失败");
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> updateRolePermissions(@PathVariable Long id, @RequestBody Map<String, List<String>> body) {
        List<String> permissions = body.get("permissions");
        roleService.updateRolePermissions(id, permissions);
        auditLogService.logSuccess(null, "admin", "UPDATE", "permission", "角色权限配置");
        return Result.success();
    }
}
