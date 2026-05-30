package com.eventhub.backend.controller;

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
    public SessionResponse createSession(
            @Valid @RequestBody SessionRequest request) {

        return sessionService.createSession(request);
    }

    @GetMapping
    public List<SessionResponse> getAllSessions() {

        return sessionService.getAllSessions();
    }

    @GetMapping("/{id}")
    public SessionResponse getSessionById(
            @PathVariable Long id) {

        return sessionService.getSessionById(id);
    }

    @GetMapping("/event/{eventId}")
    public List<SessionResponse> getSessionsByEvent(
            @PathVariable Long eventId) {

        return sessionService.getSessionsByEvent(eventId);
    }
}