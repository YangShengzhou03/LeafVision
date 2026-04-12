package com.leafvision.task;

import com.leafvision.service.AlertService;
import com.leafvision.service.ServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SyncTask {

    private static final Logger log = LoggerFactory.getLogger(SyncTask.class);

    private final ServerService serverService;
    private final AlertService alertService;

    @Value("${leafvision.sync.enabled:true}")
    private boolean syncEnabled;

    public SyncTask(ServerService serverService, AlertService alertService) {
        this.serverService = serverService;
        this.alertService = alertService;
    }

    @Scheduled(fixedDelayString = "${leafvision.sync.interval:60000}")
    public void syncData() {
        if (!syncEnabled) {
            return;
        }
        
        log.info("Starting scheduled sync task...");
        
        try {
            log.debug("Refreshing server status...");
            serverService.refreshStatus();
            
            log.debug("Syncing alerts from Alertmanager...");
            alertService.syncAlertsFromAlertmanager();
            
            log.debug("Syncing rules from Prometheus...");
            alertService.syncRulesFromPrometheus();
            
            log.info("Scheduled sync task completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled sync task", e);
        }
    }
}
