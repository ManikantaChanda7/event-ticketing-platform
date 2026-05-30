package com.eventhub.backend.service;

import com.eventhub.backend.dto.OrganizerRequest;
import com.eventhub.backend.dto.OrganizerResponse;

import java.util.List;

public interface OrganizerService {

    OrganizerResponse createOrganizer(OrganizerRequest request);

    List<OrganizerResponse> getAllOrganizers();

    OrganizerResponse getOrganizerById(Long id);
}