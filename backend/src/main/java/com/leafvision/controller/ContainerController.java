package com.leafvision.controller;

import com.leafvision.entity.Result;
import com.leafvision.entity.Container;
import com.leafvision.service.ContainerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/containers")
@CrossOrigin(origins = "*")
public class ContainerController {

    private final ContainerService containerService;

    public ContainerController(ContainerService containerService) {
        this.containerService = containerService;
    }

    @GetMapping
    public Result<List<Container>> getAllContainers() {
        return Result.success(containerService.getAllContainers());
    }
}
