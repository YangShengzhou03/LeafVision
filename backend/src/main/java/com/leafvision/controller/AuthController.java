package com.leafvision.controller;

import com.leafvision.entity.Result;
import com.leafvision.entity.User;
import com.leafvision.service.UserService;
import com.leafvision.service.AuditLogService;
import org.springframework.web.bind.annotation.*;

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
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
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
        
        boolean passwordValid = userService.verifyPassword(password, user.getPassword());
        if (!passwordValid && password.equals(user.getPassword())) {
            passwordValid = true;
        }
        
        if (!passwordValid) {
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
    public Result<Void> logout() {
        return Result.success();
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> data) {
        String password = data.get("password");
        String email = data.get("email");
        
        if (password == null || password.length() < 6) {
            return Result.error(400, "密码长度不能少于6位");
        }
        if (email == null || email.trim().isEmpty()) {
            return Result.error(400, "邮箱不能为空");
        }
        
        try {
            User user = new User();
            String generatedUsername = userService.generateRandomUsername();
            user.setUsername(generatedUsername);
            user.setPassword(password);
            user.setName(generatedUsername);
            user.setEmail(email.trim());
            
            User created = userService.registerUser(user);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", created.getId());
            result.put("username", created.getUsername());
            result.put("name", created.getName());
            
            auditLogService.logSuccess(created.getId(), generatedUsername, "REGISTER", "auth", "注册成功");
            return Result.success(result);
        } catch (RuntimeException e) {
            auditLogService.logFail(null, null, "REGISTER", "auth", e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public Result<Map<String, String>> forgotPassword(@RequestBody Map<String, String> data) {
        String email = data.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return Result.error(400, "邮箱不能为空");
        }
        
        User user = userService.getUserByEmail(email.trim());
        if (user == null) {
            return Result.error(404, "该邮箱未注册");
        }
        
        String token = userService.createPasswordResetToken(user.getId());
        
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("message", "重置链接已发送到您的邮箱");
        
        auditLogService.logSuccess(user.getId(), user.getUsername(), "FORGOT_PASSWORD", "auth", "请求密码重置");
        return Result.success(result);
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> data) {
        String token = data.get("token");
        String password = data.get("password");
        
        if (token == null || token.trim().isEmpty()) {
            return Result.error(400, "重置令牌不能为空");
        }
        if (password == null || password.length() < 6) {
            return Result.error(400, "密码长度不能少于6位");
        }
        
        try {
            userService.resetPassword(token.trim(), password);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> data) {
        String userIdStr = data.get("userId");
        String oldPassword = data.get("oldPassword");
        String newPassword = data.get("newPassword");
        
        if (userIdStr == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.error(400, "原密码不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error(400, "新密码长度不能少于6位");
        }
        
        try {
            Long userId = Long.parseLong(userIdStr);
            userService.changePassword(userId, oldPassword, newPassword);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
