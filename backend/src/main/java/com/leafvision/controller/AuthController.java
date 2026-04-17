package com.leafvision.controller;

import com.leafvision.entity.Result;
import com.leafvision.entity.User;
import com.leafvision.service.UserService;
import com.leafvision.service.AuditLogService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public AuthController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        if (username == null || password == null) {
            return Result.error(400, "用户名和密码不能为空");
        }
        
        User user = userService.getUserByUsername(username);
        if (user == null) {
            auditLogService.logFail(null, username, "LOGIN", "auth", "用户不存在");
            return Result.error(401, "用户名或密码错误");
        }
        
        if (!password.equals(user.getPassword())) {
            auditLogService.logFail(user.getId(), username, "LOGIN", "auth", "密码错误");
            return Result.error(401, "用户名或密码错误");
        }
        
        if (user.getStatus() != 1) {
            auditLogService.logFail(user.getId(), username, "LOGIN", "auth", "账号已禁用");
            return Result.error(403, "账号已被禁用");
        }
        
        List<String> permissions = userService.getUserPermissions(user.getRoleId());
        userService.updateLastLogin(user.getId());
        
        auditLogService.logSuccess(user.getId(), username, "LOGIN", "auth", "登录成功");
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("name", user.getName());
        result.put("role", user.getRoleCode());
        result.put("permissions", permissions);
        
        return Result.success(result);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        return Result.success();
    }
}
