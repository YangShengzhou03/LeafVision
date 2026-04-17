package com.leafvision.controller;

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
}
