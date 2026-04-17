package com.leafvision.controller;

import com.leafvision.entity.Log;
import com.leafvision.entity.Result;
import com.leafvision.service.LogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public Result<List<Log>> getLogs(
            @RequestParam(required = false) Long serverId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "100") Integer limit) {
        
        List<Log> logs = logService.searchLogs(serverId, level, keyword, startTime, endTime, limit);
        return Result.success(logs);
    }
}
