package com.leafvision.service;

import com.leafvision.entity.Role;
import com.leafvision.entity.Permission;
import com.leafvision.mapper.RoleMapper;
import com.leafvision.mapper.PermissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public RoleService(RoleMapper roleMapper, PermissionMapper permissionMapper) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    public List<Role> getAllRoles() {
        List<Role> roles = roleMapper.findAll();
        for (Role role : roles) {
            List<String> permissionCodes = permissionMapper.findCodesByRoleId(role.getId());
            role.setPermissions(permissionCodes);
        }
        return roles;
    }

    public Role getRoleById(Long id) {
        Role role = roleMapper.findById(id);
        if (role != null) {
            List<String> permissionCodes = permissionMapper.findCodesByRoleId(id);
            role.setPermissions(permissionCodes);
        }
        return role;
    }

    @Transactional
    public Role createRole(Role role) {
        role.setStatus(1);
        roleMapper.insert(role);
        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            updateRolePermissions(role.getId(), role.getPermissions());
        }
        return role;
    }

    @Transactional
    public Role updateRole(Role role) {
        roleMapper.update(role);
        if (role.getPermissions() != null) {
            updateRolePermissions(role.getId(), role.getPermissions());
        }
        return getRoleById(role.getId());
    }

    @Transactional
    public boolean deleteRole(Long id) {
        Role role = roleMapper.findById(id);
        if (role != null && "ADMIN".equals(role.getRoleCode())) {
            return false;
        }
        permissionMapper.deleteByRoleId(id);
        return roleMapper.delete(id) > 0;
    }

    @Transactional
    public void updateRolePermissions(Long roleId, List<String> permissionCodes) {
        permissionMapper.deleteByRoleId(roleId);
        if (permissionCodes != null && !permissionCodes.isEmpty()) {
            List<Permission> allPermissions = permissionMapper.findAll();
            for (String code : permissionCodes) {
                Permission perm = allPermissions.stream()
                    .filter(p -> code.equals(p.getPermissionCode()))
                    .findFirst()
                    .orElse(null);
                if (perm != null) {
                    permissionMapper.addRolePermission(roleId, perm.getId());
                }
            }
        }
    }

    public List<Permission> getAllPermissions() {
        return permissionMapper.findAll();
    }
}
