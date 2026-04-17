package com.leafvision.mapper;

import com.leafvision.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {
    
    @Select("SELECT * FROM role WHERE id = #{id}")
    Role findById(@Param("id") Long id);
    
    @Select("SELECT * FROM role ORDER BY id")
    List<Role> findAll();
    
    @Select("SELECT * FROM role WHERE role_code = #{roleCode}")
    Role findByCode(@Param("roleCode") String roleCode);
    
    @Insert("INSERT INTO role (role_code, role_name, description, status) VALUES (#{roleCode}, #{roleName}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Role role);
    
    @Update("UPDATE role SET role_code = #{roleCode}, role_name = #{roleName}, description = #{description}, status = #{status} WHERE id = #{id}")
    int update(Role role);
    
    @Delete("DELETE FROM role WHERE id = #{id}")
    int delete(@Param("id") Long id);
}
