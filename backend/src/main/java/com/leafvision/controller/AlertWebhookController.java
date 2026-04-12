package com.leafvision.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.leafvision.entity.Result;
import com.leafvision.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertWebhookController {

    private static final Logger log = LoggerFactory.getLogger(AlertWebhookController.class);

    private final AlertService alertService;

    public AlertWebhookController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping("/webhook")
    public Result<Void> receiveAlert(@RequestBody String payload) {
        log.info("Received alert webhook: {}", payload);
        
        try {
            JSONObject alertData = JSON.parseObject(payload);
            alertService.processWebhookAlert(alertData);
            return Result.success();
        } catch (Exception e) {
            log.error("Failed to process alert webhook", e);
            return Result.error(500, "Failed to process alert: " + e.getMessage());
        }
    }
}
