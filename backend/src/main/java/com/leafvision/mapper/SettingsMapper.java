package com.leafvision.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leafvision.entity.Settings;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SettingsMapper extends BaseMapper<Settings> {
    
    @Select("SELECT * FROM settings WHERE setting_key = #{key}")
    Settings findByKey(@Param("key") String key);
    
    @Select("SELECT * FROM settings")
    List<Settings> findAll();
    
    @Update("UPDATE settings SET setting_value = #{settingValue}, updated_at = NOW() WHERE setting_key = #{settingKey}")
    int updateByKey(Settings settings);
    
    @Insert("INSERT INTO settings (setting_key, setting_value, setting_type, description, created_at, updated_at) " +
            "VALUES (#{settingKey}, #{settingValue}, #{settingType}, #{description}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE setting_value = #{settingValue}, updated_at = NOW()")
    int insertOrUpdate(Settings settings);
    
    @Delete("DELETE FROM settings WHERE setting_key = #{key}")
    int deleteByKey(@Param("key") String key);
}
