package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.OrganizerRequest;
import com.eventhub.backend.dto.OrganizerResponse;
import com.eventhub.backend.entity.Organizer;
import com.eventhub.backend.exception.ResourceNotFoundException;
import com.eventhub.backend.repository.OrganizerRepository;
import com.eventhub.backend.service.OrganizerService;
import org.springframework.stereotype.Service;

import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
public class OrganizerServiceImpl implements OrganizerService {

        private final OrganizerRepository organizerRepository;
        private final UserRepository userRepository;

        public OrganizerServiceImpl(
                        OrganizerRepository organizerRepository,
                        UserRepository userRepository) {

                this.organizerRepository = organizerRepository;
                this.userRepository = userRepository;
        }

        @Override
        public OrganizerResponse createOrganizer(
                        OrganizerRequest request) {

                Organizer organizer = new Organizer();

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (organizerRepository.findByUser(user).isPresent()) {
                        throw new RuntimeException(
                                        "User already has an organizer profile");
                }

                organizer.setOrganizationName(
                                request.getOrganizationName());

                organizer.setWebsite(
                                request.getWebsite());

                organizer.setDescription(
                                request.getDescription());

                organizer.setUser(user);

                user.setRole(Role.ORGANIZER);
                userRepository.save(user);

                organizer = organizerRepository.save(organizer);

                return mapToResponse(organizer);
        }

        @Override
        public List<OrganizerResponse> getAllOrganizers() {

                return organizerRepository.findAll()
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        public OrganizerResponse getOrganizerById(Long id) {

                Organizer organizer = organizerRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Organizer not found"));

                return mapToResponse(organizer);
        }

        private OrganizerResponse mapToResponse(
                        Organizer organizer) {

                return OrganizerResponse.builder()
                                .id(organizer.getId())
                                .organizationName(
                                                organizer.getOrganizationName())
                                .website(organizer.getWebsite())
                                .description(
                                                organizer.getDescription())
                                .verified(organizer.getVerified())
                                .build();
        }
}