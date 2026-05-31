package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public ApiResponse<String> test() {
        return new ApiResponse<>(true, "Test endpoint successful", "JWT Protected Endpoint");
    }
}