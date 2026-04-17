package com.leafvision.mapper;

import com.leafvision.entity.AuditLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AuditLogMapper {
    
    @Select("<script>" +
            "SELECT * FROM audit_log WHERE 1=1 " +
            "<if test='module != null and module != \"\"'> AND module = #{module}</if>" +
            "<if test='operation != null and operation != \"\"'> AND operation = #{operation}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (username LIKE CONCAT('%', #{keyword}, '%') OR target_name LIKE CONCAT('%', #{keyword}, '%'))</if>" +
            " ORDER BY created_at DESC LIMIT 500" +
            "</script>")
    List<AuditLog> search(@Param("module") String module, @Param("operation") String operation, @Param("keyword") String keyword);
    
    @Insert("INSERT INTO audit_log (user_id, username, operation, module, target_type, target_id, target_name, detail, ip_address, user_agent, status) VALUES (#{userId}, #{username}, #{operation}, #{module}, #{targetType}, #{targetId}, #{targetName}, #{detail}, #{ipAddress}, #{userAgent}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditLog log);
}
