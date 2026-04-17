package com.leafvision.mapper;

import com.leafvision.entity.Trace;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TraceMapper {
    
    @Select("<script>" +
            "SELECT t.*, s.name as server_name FROM traces t " +
            "LEFT JOIN servers s ON t.server_id = s.id " +
            "WHERE 1=1 " +
            "<if test='service != null and service != \"\"'> AND t.service LIKE CONCAT('%', #{service}, '%')</if>" +
            "<if test='operation != null and operation != \"\"'> AND t.operation LIKE CONCAT('%', #{operation}, '%')</if>" +
            "<if test='minDuration != null and minDuration != \"\"'> AND t.duration &gt;= #{minDuration}</if>" +
            "<if test='maxDuration != null and maxDuration != \"\"'> AND t.duration &lt;= #{maxDuration}</if>" +
            " ORDER BY t.start_time DESC LIMIT #{limit}" +
            "</script>")
    List<Trace> search(@Param("service") String service, 
                       @Param("operation") String operation,
                       @Param("minDuration") Long minDuration,
                       @Param("maxDuration") Long maxDuration,
                       @Param("limit") Integer limit);
    
    @Select("SELECT * FROM traces WHERE trace_id = #{traceId}")
    Trace findByTraceId(@Param("traceId") String traceId);
    
    @Insert("INSERT INTO traces (trace_id, service, operation, duration, span_count, status, start_time, server_id) VALUES (#{traceId}, #{service}, #{operation}, #{duration}, #{spanCount}, #{status}, #{startTime}, #{serverId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Trace trace);
    
    @Delete("DELETE FROM traces WHERE created_at < DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int deleteOldTraces(@Param("days") int days);
}
