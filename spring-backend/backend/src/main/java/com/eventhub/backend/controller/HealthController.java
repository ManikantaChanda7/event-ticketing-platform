package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResponse<String> health() {
        return new ApiResponse<>(true, "Health check successful", "Auto Reload Working");
    }
}