package com.leafvision.mapper;

import com.leafvision.entity.Log;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LogMapper {
    
    @Select("<script>" +
            "SELECT l.*, s.name as server_name FROM logs l " +
            "LEFT JOIN servers s ON l.server_id = s.id " +
            "WHERE 1=1 " +
            "<if test='serverId != null'> AND l.server_id = #{serverId}</if>" +
            "<if test='level != null and level != \"all\"'> AND l.level = #{level}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (l.message LIKE CONCAT('%', #{keyword}, '%') OR l.source LIKE CONCAT('%', #{keyword}, '%'))</if>" +
            "<if test='startTime != null and startTime != \"\"'> AND l.time &gt;= #{startTime}</if>" +
            "<if test='endTime != null and endTime != \"\"'> AND l.time &lt;= #{endTime}</if>" +
            " ORDER BY l.time DESC LIMIT #{limit}" +
            "</script>")
    List<Log> search(@Param("serverId") Long serverId, 
                     @Param("level") String level, 
                     @Param("keyword") String keyword,
                     @Param("startTime") String startTime,
                     @Param("endTime") String endTime,
                     @Param("limit") Integer limit);
    
    @Insert("INSERT INTO logs (server_id, level, source, message, time) VALUES (#{serverId}, #{level}, #{source}, #{message}, #{time})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Log log);
    
    @Delete("DELETE FROM logs WHERE created_at < DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int deleteOldLogs(@Param("days") int days);
}
