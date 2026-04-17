package com.leafvision.mapper;

import com.leafvision.entity.PasswordResetToken;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PasswordResetTokenMapper {
    
    @Select("SELECT * FROM password_reset_token WHERE token = #{token}")
    PasswordResetToken findByToken(@Param("token") String token);
    
    @Select("SELECT * FROM password_reset_token WHERE user_id = #{userId} AND used = 0 AND expires_at > NOW() ORDER BY created_at DESC LIMIT 1")
    PasswordResetToken findValidByUserId(@Param("userId") Long userId);
    
    @Insert("INSERT INTO password_reset_token (user_id, token, expires_at, used) VALUES (#{userId}, #{token}, #{expiresAt}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PasswordResetToken token);
    
    @Update("UPDATE password_reset_token SET used = 1 WHERE id = #{id}")
    int markAsUsed(@Param("id") Long id);
    
    @Delete("DELETE FROM password_reset_token WHERE expires_at < NOW() OR used = 1")
    int cleanExpiredTokens();
}
