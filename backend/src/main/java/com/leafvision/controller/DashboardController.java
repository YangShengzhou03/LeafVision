package com.leafvision.controller;

import com.leafvision.entity.DashboardData;
import com.leafvision.entity.Result;
import com.leafvision.service.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public Result<DashboardData> getOverview() {
        return Result.success(dashboardService.getDashboardData());
    }
}
