package com.leafvision.controller;

import com.leafvision.entity.Result;
import com.leafvision.service.SettingsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public Result<Map<String, Object>> getSettings() {
        return Result.success(settingsService.getSettings());
    }

    @PutMapping
    public Result<Void> updateSettings(@RequestBody Map<String, Object> settings) {
        settingsService.updateSettings(settings);
        return Result.success();
    }
}
