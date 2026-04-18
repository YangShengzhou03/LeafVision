package com.leafvision.controller;

import com.alibaba.fastjson.JSONObject;
import com.leafvision.entity.Result;
import com.leafvision.entity.Service;
import com.leafvision.service.ServiceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public Result<List<Service>> getAllServices() {
        return Result.success(serviceService.getAllServices());
    }

    @GetMapping("/{id}")
    public Result<Service> getServiceById(@PathVariable Long id) {
        Service service = serviceService.getServiceById(id);
        if (service == null) {
            return Result.error(404, "服务不存在");
        }
        return Result.success(service);
    }

    @PostMapping
    public Result<Service> createService(@RequestBody Service service) {
        Service created = serviceService.createService(service);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Service> updateService(@PathVariable Long id, @RequestBody Service service) {
        Service updated = serviceService.updateService(id, service);
        if (updated == null) {
            return Result.error(404, "服务不存在");
        }
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteService(@PathVariable Long id) {
        boolean deleted = serviceService.deleteService(id);
        if (!deleted) {
            return Result.error(404, "服务不存在");
        }
        return Result.success(null);
    }

    @GetMapping("/{id}/metrics")
    public Result<JSONObject> getServiceMetrics(@PathVariable Long id) {
        JSONObject metrics = serviceService.getServiceMetrics(id);
        if (metrics == null) {
            return Result.error(404, "服务不存在");
        }
        return Result.success(metrics);
    }
}
