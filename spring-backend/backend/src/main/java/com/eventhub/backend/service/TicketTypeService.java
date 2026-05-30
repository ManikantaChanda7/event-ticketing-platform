package com.eventhub.backend.service;

import com.eventhub.backend.dto.TicketTypeRequest;
import com.eventhub.backend.dto.TicketTypeResponse;

import java.util.List;

public interface TicketTypeService {

    TicketTypeResponse createTicketType(
            TicketTypeRequest request);

    List<TicketTypeResponse> getAllTicketTypes();

    TicketTypeResponse getTicketTypeById(Long id);

    List<TicketTypeResponse> getTicketTypesBySession(
            Long sessionId);
}