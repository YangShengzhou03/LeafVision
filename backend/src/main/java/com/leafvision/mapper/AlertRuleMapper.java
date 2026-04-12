package com.leafvision.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leafvision.entity.AlertRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AlertRuleMapper extends BaseMapper<AlertRule> {
    
    @Select("SELECT * FROM alert_rules WHERE enabled = #{enabled}")
    List<AlertRule> findByEnabled(Boolean enabled);
    
    @Select("SELECT * FROM alert_rules WHERE server_id = #{serverId}")
    List<AlertRule> findByServerId(Long serverId);
    
    @Select("SELECT * FROM alert_rules WHERE group_name = #{groupName}")
    List<AlertRule> findByGroupName(String groupName);
}
