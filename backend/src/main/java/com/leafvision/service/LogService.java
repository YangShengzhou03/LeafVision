package com.leafvision.service;

import com.leafvision.entity.Log;
import com.leafvision.mapper.LogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {

    private static final Logger log = LoggerFactory.getLogger(LogService.class);

    private final LogMapper logMapper;

    public LogService(LogMapper logMapper) {
        this.logMapper = logMapper;
    }

    public List<Log> searchLogs(Long serverId, String level, String keyword, 
                                String startTime, String endTime, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        if (limit > 1000) {
            limit = 1000;
        }
        return logMapper.search(serverId, level, keyword, startTime, endTime, limit);
    }

    public void addLog(Long serverId, String level, String source, String message, String time) {
        Log logEntry = new Log();
        logEntry.setServerId(serverId);
        logEntry.setLevel(level);
        logEntry.setSource(source);
        logEntry.setMessage(message);
        logEntry.setTime(time);
        logMapper.insert(logEntry);
    }

    public void cleanOldLogs(int days) {
        log.info("Cleaning logs older than {} days", days);
        logMapper.deleteOldLogs(days);
    }
}
