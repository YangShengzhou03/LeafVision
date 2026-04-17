package com.leafvision.service;

import com.leafvision.entity.AuditLog;
import com.leafvision.mapper.AuditLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    public AuditLogService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public List<AuditLog> searchLogs(String module, String operation, String keyword) {
        return auditLogMapper.search(module, operation, keyword);
    }

    public void log(Long userId, String username, String operation, String module, 
                   String targetType, String targetId, String targetName, 
                   String detail, String ipAddress, String userAgent, Integer status) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperation(operation);
        log.setModule(module);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setTargetName(targetName);
        log.setDetail(detail);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setStatus(status);
        auditLogMapper.insert(log);
    }

    public void logSuccess(Long userId, String username, String operation, String module, String targetName) {
        log(userId, username, operation, module, null, null, targetName, null, null, null, 1);
    }

    public void logFail(Long userId, String username, String operation, String module, String detail) {
        log(userId, username, operation, module, null, null, null, detail, null, null, 0);
    }
}
