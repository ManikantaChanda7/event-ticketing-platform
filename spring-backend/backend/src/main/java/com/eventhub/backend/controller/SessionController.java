package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import com.eventhub.backend.dto.SessionRequest;
import com.eventhub.backend.dto.SessionResponse;
import com.eventhub.backend.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(
            SessionService sessionService) {

        this.sessionService = sessionService;
    }

    @PostMapping
    public ApiResponse<SessionResponse> createSession(
            @Valid @RequestBody SessionRequest request) {
        SessionResponse response = sessionService.createSession(request);
        return new ApiResponse<>(true, "Session created successfully", response);
    }

    @GetMapping
    public ApiResponse<List<SessionResponse>> getAllSessions() {
        List<SessionResponse> response = sessionService.getAllSessions();
        return new ApiResponse<>(true, "All sessions fetched successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<SessionResponse> getSessionById(
            @PathVariable Long id) {
        SessionResponse response = sessionService.getSessionById(id);
        return new ApiResponse<>(true, "Session fetched successfully", response);
    }

    @GetMapping("/event/{eventId}")
    public ApiResponse<List<SessionResponse>> getSessionsByEvent(
            @PathVariable Long eventId) {
        List<SessionResponse> response = sessionService.getSessionsByEvent(eventId);
        return new ApiResponse<>(true, "Sessions by event fetched successfully", response);
    }
}