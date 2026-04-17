package com.leafvision.service;

import com.leafvision.entity.Server;
import com.leafvision.entity.ServerGroup;
import com.leafvision.exception.BusinessException;
import com.leafvision.mapper.ServerGroupMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServerGroupService {

    private final ServerGroupMapper groupMapper;

    public ServerGroupService(ServerGroupMapper groupMapper) {
        this.groupMapper = groupMapper;
    }

    public List<ServerGroup> getAllGroups() {
        return groupMapper.findAll();
    }

    public ServerGroup getGroupById(Long id) {
        ServerGroup group = groupMapper.findById(id);
        if (group != null) {
            group.setServers(groupMapper.findServersByGroupId(id));
        }
        return group;
    }

    public List<ServerGroup> getGroupTree() {
        List<ServerGroup> allGroups = groupMapper.findAll();
        Map<Long, ServerGroup> groupMap = allGroups.stream()
                .collect(Collectors.toMap(ServerGroup::getId, g -> g));
        
        List<ServerGroup> rootGroups = new ArrayList<>();
        for (ServerGroup group : allGroups) {
            if (group.getParentId() == null || group.getParentId() == 0) {
                rootGroups.add(group);
            } else {
                ServerGroup parent = groupMap.get(group.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(group);
                }
            }
        }
        return rootGroups;
    }

    public ServerGroup createGroup(ServerGroup group) {
        if (group.getParentId() == null) {
            group.setParentId(0L);
        }
        if (group.getSortOrder() == null) {
            group.setSortOrder(0);
        }
        if (group.getStatus() == null) {
            group.setStatus(1);
        }
        groupMapper.insert(group);
        return group;
    }

    public ServerGroup updateGroup(ServerGroup group) {
        ServerGroup existing = groupMapper.findById(group.getId());
        if (existing == null) {
            throw new BusinessException("分组不存在");
        }
        groupMapper.update(group);
        return groupMapper.findById(group.getId());
    }

    public boolean deleteGroup(Long id) {
        groupMapper.clearGroupRelations(id);
        return groupMapper.delete(id) > 0;
    }

    public void addServerToGroup(Long serverId, Long groupId) {
        groupMapper.addServerToGroup(serverId, groupId);
    }

    public void removeServerFromGroup(Long serverId, Long groupId) {
        groupMapper.removeServerFromGroup(serverId, groupId);
    }

    public List<Server> getServersByGroup(Long groupId) {
        return groupMapper.findServersByGroupId(groupId);
    }
}
