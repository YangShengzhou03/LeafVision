package com.leafvision.mapper;

import com.leafvision.entity.Permission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PermissionMapper {
    
    @Select("SELECT * FROM permission WHERE id = #{id}")
    Permission findById(@Param("id") Long id);
    
    @Select("SELECT * FROM permission WHERE status = 1 ORDER BY sort_order")
    List<Permission> findAll();
    
    @Select("SELECT p.* FROM permission p INNER JOIN role_permission rp ON p.id = rp.permission_id WHERE rp.role_id = #{roleId} AND p.status = 1")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);
    
    @Select("SELECT p.permission_code FROM permission p INNER JOIN role_permission rp ON p.id = rp.permission_id WHERE rp.role_id = #{roleId} AND p.status = 1")
    List<String> findCodesByRoleId(@Param("roleId") Long roleId);
    
    @Insert("INSERT INTO role_permission (role_id, permission_id) VALUES (#{roleId}, #{permissionId})")
    int addRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
    
    @Delete("DELETE FROM role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}
