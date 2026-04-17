package com.leafvision.mapper;

import com.leafvision.entity.ServerGroup;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ServerGroupMapper {
    
    @Select("SELECT * FROM server_group WHERE id = #{id}")
    ServerGroup findById(@Param("id") Long id);
    
    @Select("SELECT * FROM server_group WHERE status = 1 ORDER BY sort_order, id")
    List<ServerGroup> findAll();
    
    @Select("SELECT * FROM server_group WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort_order, id")
    List<ServerGroup> findByParentId(@Param("parentId") Long parentId);
    
    @Insert("INSERT INTO server_group (name, description, parent_id, sort_order, status) VALUES (#{name}, #{description}, #{parentId}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ServerGroup group);
    
    @Update("UPDATE server_group SET name = #{name}, description = #{description}, parent_id = #{parentId}, sort_order = #{sortOrder}, status = #{status} WHERE id = #{id}")
    int update(ServerGroup group);
    
    @Delete("DELETE FROM server_group WHERE id = #{id}")
    int delete(@Param("id") Long id);
    
    @Insert("INSERT INTO server_group_relation (server_id, group_id) VALUES (#{serverId}, #{groupId})")
    int addServerToGroup(@Param("serverId") Long serverId, @Param("groupId") Long groupId);
    
    @Delete("DELETE FROM server_group_relation WHERE server_id = #{serverId} AND group_id = #{groupId}")
    int removeServerFromGroup(@Param("serverId") Long serverId, @Param("groupId") Long groupId);
    
    @Delete("DELETE FROM server_group_relation WHERE group_id = #{groupId}")
    int clearGroupRelations(@Param("groupId") Long groupId);
    
    @Select("SELECT s.* FROM servers s INNER JOIN server_group_relation r ON s.id = r.server_id WHERE r.group_id = #{groupId}")
    List<com.leafvision.entity.Server> findServersByGroupId(@Param("groupId") Long groupId);
}
