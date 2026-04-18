package com.leafvision.service;

import com.leafvision.entity.PasswordResetToken;
import com.leafvision.entity.User;
import com.leafvision.exception.BusinessException;
import com.leafvision.mapper.PasswordResetTokenMapper;
import com.leafvision.mapper.UserMapper;
import com.leafvision.mapper.PermissionMapper;
import com.leafvision.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PermissionMapper permissionMapper;
    private final PasswordResetTokenMapper tokenMapper;

    public UserService(UserMapper userMapper, PermissionMapper permissionMapper, PasswordResetTokenMapper tokenMapper) {
        this.userMapper = userMapper;
        this.permissionMapper = permissionMapper;
        this.tokenMapper = tokenMapper;
    }

    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    public User getUserById(Long id) {
        return userMapper.findById(id);
    }

    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public User getUserByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    public User createUser(User user) {
        user.setStatus(1);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        }
        userMapper.insert(user);
        return user;
    }

    public User registerUser(User user) {
        User existingByUsername = userMapper.findByUsername(user.getUsername());
        if (existingByUsername != null) {
            throw new BusinessException("用户名已存在");
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            User existingByEmail = userMapper.findByEmail(user.getEmail());
            if (existingByEmail != null) {
                throw new BusinessException("邮箱已被注册");
            }
        }
        if (user.getRoleId() == null) {
            user.setRoleId(3L);
        }
        return createUser(user);
    }

    public User updateUser(User user) {
        User existing = userMapper.findById(user.getId());
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty() 
                && !user.getPassword().equals(existing.getPassword())) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        } else {
            user.setPassword(existing.getPassword());
        }
        userMapper.update(user);
        return userMapper.findById(user.getId());
    }

    public boolean deleteUser(Long id) {
        return userMapper.delete(id) > 0;
    }

    public List<String> getUserPermissions(Long roleId) {
        return permissionMapper.findCodesByRoleId(roleId);
    }

    public void updateLastLogin(Long id) {
        userMapper.updateLastLogin(id);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return PasswordUtil.matches(rawPassword, encodedPassword);
    }

    public String createPasswordResetToken(Long userId) {
        tokenMapper.cleanExpiredTokens();
        String token = UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(userId);
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        tokenMapper.insert(resetToken);
        return token;
    }

    public boolean resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenMapper.findByToken(token);
        if (resetToken == null) {
            throw new BusinessException("无效的重置令牌");
        }
        if (resetToken.getUsed() == 1) {
            throw new BusinessException("重置令牌已使用");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("重置令牌已过期");
        }
        String encodedPassword = PasswordUtil.encode(newPassword);
        userMapper.updatePassword(resetToken.getUserId(), encodedPassword);
        tokenMapper.markAsUsed(resetToken.getId());
        return true;
    }

    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!verifyPassword(oldPassword, user.getPassword()) && !oldPassword.equals(user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        String encodedPassword = PasswordUtil.encode(newPassword);
        userMapper.updatePassword(userId, encodedPassword);
        return true;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!verifyPassword(oldPassword, user.getPassword()) && !oldPassword.equals(user.getPassword())) {
            return false;
        }
        String encodedPassword = PasswordUtil.encode(newPassword);
        userMapper.updatePassword(user.getId(), encodedPassword);
        return true;
    }

    public String getUsernameFromToken(String token) {
        return null;
    }

    public String generateRandomUsername() {
        Random random = new Random();
        StringBuilder username;
        int attempts = 0;
        int maxAttempts = 100;
        
        do {
            username = new StringBuilder("u");
            for (int i = 0; i < 6; i++) {
                username.append(random.nextInt(10));
            }
            attempts++;
            if (attempts >= maxAttempts) {
                username.append(System.currentTimeMillis() % 10000);
                break;
            }
        } while (userMapper.findByUsername(username.toString()) != null);
        
        return username.toString();
    }
}
