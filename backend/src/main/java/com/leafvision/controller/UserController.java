package com.leafvision.controller;

import com.leafvision.entity.Result;
import com.leafvision.entity.User;
import com.leafvision.entity.AuditLog;
import com.leafvision.service.UserService;
import com.leafvision.service.AuditLogService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public UserController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public Result<List<User>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    @PostMapping
    public Result<User> createUser(@RequestBody User user, HttpServletRequest request) {
        try {
            User created = userService.createUser(user);
            auditLogService.logSuccess(null, "admin", "CREATE", "user", user.getUsername());
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        try {
            user.setId(id);
            User updated = userService.updateUser(user);
            auditLogService.logSuccess(null, "admin", "UPDATE", "user", user.getUsername());
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if (userService.deleteUser(id)) {
            auditLogService.logSuccess(null, "admin", "DELETE", "user", user.getUsername());
            return Result.success();
        }
        return Result.error(400, "删除失败");
    }
}
