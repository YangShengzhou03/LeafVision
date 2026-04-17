package com.leafvision.controller;

import com.leafvision.entity.Result;
import com.leafvision.entity.Permission;
import com.leafvision.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "*")
public class PermissionController {

    private final RoleService roleService;

    public PermissionController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public Result<List<Permission>> getAllPermissions() {
        return Result.success(roleService.getAllPermissions());
    }
}
