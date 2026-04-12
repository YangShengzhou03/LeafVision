package com.leafvision.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leafvision.entity.Alert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface AlertMapper extends BaseMapper<Alert> {
    
    @Select("SELECT * FROM alerts WHERE status = #{status}")
    List<Alert> findByStatus(String status);
    
    @Select("SELECT * FROM alerts WHERE server_id = #{serverId}")
    List<Alert> findByServerId(Long serverId);
    
    @Select("SELECT * FROM alerts WHERE fingerprint = #{fingerprint}")
    Optional<Alert> findByFingerprint(String fingerprint);
    
    @Select("SELECT * FROM alerts WHERE starts_at > #{time}")
    List<Alert> findByStartsAtAfter(LocalDateTime time);
}
