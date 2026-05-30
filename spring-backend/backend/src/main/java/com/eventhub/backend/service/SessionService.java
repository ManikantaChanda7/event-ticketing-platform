package com.eventhub.backend.service;

import com.eventhub.backend.dto.SessionRequest;
import com.eventhub.backend.dto.SessionResponse;

import java.util.List;

public interface SessionService {

    SessionResponse createSession(SessionRequest request);

    List<SessionResponse> getAllSessions();

    SessionResponse getSessionById(Long id);

    List<SessionResponse> getSessionsByEvent(Long eventId);
}