package com.leafvision.mapper;

import com.leafvision.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    
    @Select("SELECT u.*, r.role_code, r.role_name FROM user u LEFT JOIN role r ON u.role_id = r.id WHERE u.id = #{id}")
    User findById(@Param("id") Long id);
    
    @Select("SELECT u.*, r.role_code, r.role_name FROM user u LEFT JOIN role r ON u.role_id = r.id")
    List<User> findAll();
    
    @Select("SELECT u.*, r.role_code, r.role_name FROM user u LEFT JOIN role r ON u.role_id = r.id WHERE u.username = #{username}")
    User findByUsername(@Param("username") String username);
    
    @Insert("INSERT INTO user (username, password, name, email, phone, role_id, status) VALUES (#{username}, #{password}, #{name}, #{email}, #{phone}, #{roleId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Update("UPDATE user SET username = #{username}, name = #{name}, email = #{email}, phone = #{phone}, role_id = #{roleId}, status = #{status} WHERE id = #{id}")
    int update(User user);
    
    @Delete("DELETE FROM user WHERE id = #{id}")
    int delete(@Param("id") Long id);
    
    @Update("UPDATE user SET last_login_at = NOW() WHERE id = #{id}")
    int updateLastLogin(@Param("id") Long id);
}
