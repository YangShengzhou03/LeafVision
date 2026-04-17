package com.leafvision.controller;

import com.leafvision.entity.Result;
import com.leafvision.entity.Trace;
import com.leafvision.service.TraceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traces")
@CrossOrigin(origins = "*")
public class TraceController {

    private final TraceService traceService;

    public TraceController(TraceService traceService) {
        this.traceService = traceService;
    }

    @GetMapping
    public Result<List<Trace>> getTraces(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String minDuration,
            @RequestParam(required = false) String maxDuration,
            @RequestParam(defaultValue = "50") Integer limit) {
        
        List<Trace> traces = traceService.searchTraces(service, operation, minDuration, maxDuration, limit);
        return Result.success(traces);
    }
    
    @GetMapping("/{traceId}")
    public Result<Trace> getTraceByTraceId(@PathVariable String traceId) {
        Trace trace = traceService.getTraceByTraceId(traceId);
        if (trace == null) {
            return Result.error(404, "链路不存在");
        }
        return Result.success(trace);
    }
}
