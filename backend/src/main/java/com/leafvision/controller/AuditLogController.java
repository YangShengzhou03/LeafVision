package com.leafvision.controller;

import com.leafvision.entity.Result;
import com.leafvision.entity.AuditLog;
import com.leafvision.service.AuditLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public Result<List<AuditLog>> searchLogs(@RequestParam(required = false) String module,
                                             @RequestParam(required = false) String operation,
                                             @RequestParam(required = false) String keyword) {
        return Result.success(auditLogService.searchLogs(module, operation, keyword));
    }
}
