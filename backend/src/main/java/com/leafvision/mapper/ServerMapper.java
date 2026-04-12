package com.leafvision.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leafvision.entity.Server;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ServerMapper extends BaseMapper<Server> {
    
    @Select("SELECT * FROM servers WHERE type = #{type}")
    List<Server> findByType(String type);
    
    @Select("SELECT * FROM servers WHERE status = #{status}")
    List<Server> findByStatus(String status);
    
    @Select("SELECT COUNT(*) > 0 FROM servers WHERE ip = #{ip} AND port = #{port}")
    boolean existsByIpAndPort(String ip, Integer port);
}
