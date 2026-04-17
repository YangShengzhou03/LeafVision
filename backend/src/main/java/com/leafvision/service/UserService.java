package com.leafvision.service;

import com.leafvision.entity.User;
import com.leafvision.mapper.UserMapper;
import com.leafvision.mapper.PermissionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PermissionMapper permissionMapper;

    public UserService(UserMapper userMapper, PermissionMapper permissionMapper) {
        this.userMapper = userMapper;
        this.permissionMapper = permissionMapper;
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

    public User createUser(User user) {
        user.setStatus(1);
        userMapper.insert(user);
        return user;
    }

    public User updateUser(User user) {
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
}
