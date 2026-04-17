package com.leafvision.service;

import com.leafvision.entity.Trace;
import com.leafvision.mapper.TraceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraceService {

    private static final Logger log = LoggerFactory.getLogger(TraceService.class);

    private final TraceMapper traceMapper;

    public TraceService(TraceMapper traceMapper) {
        this.traceMapper = traceMapper;
    }

    public List<Trace> searchTraces(String service, String operation, 
                                    String minDuration, String maxDuration, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 50;
        }
        if (limit > 500) {
            limit = 500;
        }
        
        Long minDur = parseDuration(minDuration);
        Long maxDur = parseDuration(maxDuration);
        
        return traceMapper.search(service, operation, minDur, maxDur, limit);
    }

    public Trace getTraceByTraceId(String traceId) {
        return traceMapper.findByTraceId(traceId);
    }

    public void addTrace(Trace trace) {
        traceMapper.insert(trace);
    }

    public void cleanOldTraces(int days) {
        log.info("Cleaning traces older than {} days", days);
        traceMapper.deleteOldTraces(days);
    }

    private Long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) {
            return null;
        }
        
        try {
            duration = duration.trim().toLowerCase();
            if (duration.endsWith("ms")) {
                return Long.parseLong(duration.replace("ms", "")) * 1000;
            } else if (duration.endsWith("s")) {
                return Long.parseLong(duration.replace("s", "")) * 1000000;
            } else if (duration.endsWith("m")) {
                return Long.parseLong(duration.replace("m", "")) * 60 * 1000000;
            } else {
                return Long.parseLong(duration);
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse duration: {}", duration);
            return null;
        }
    }
}
