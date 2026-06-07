package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.OrganizerRequest;
import com.eventhub.backend.dto.OrganizerResponse;
import com.eventhub.backend.entity.Event;
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

                // Use new fields first, fall back to old ones
                String orgName = request.getOrgName() != null ? request.getOrgName() : request.getOrganizationName();
                String orgDescription = request.getOrgDescription() != null ? request.getOrgDescription() : request.getDescription();

                organizer.setOrgName(orgName);
                organizer.setOrgEmail(request.getOrgEmail());
                organizer.setOrgDescription(orgDescription);
                organizer.setPhone(request.getPhone());
                organizer.setOrganizerProfileImage(request.getOrganizerProfileImage());
                organizer.setOrganizerBannerImage(request.getOrganizerBannerImage());
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
                                ._id(organizer.getId())
                                .id(organizer.getId())
                                .user(organizer.getUser() != null ? organizer.getUser().getId() : null)
                                .phone(organizer.getPhone())
                                .organizerProfileImage(organizer.getOrganizerProfileImage())
                                .orgName(organizer.getOrgName())
                                .orgEmail(organizer.getOrgEmail())
                                .orgDescription(organizer.getOrgDescription())
                                .organizerBannerImage(organizer.getOrganizerBannerImage())
                                .orgSpecialities(organizer.getOrgSpecialities() != null
                                                ? organizer.getOrgSpecialities().stream().toList()
                                                : null)
                                .averageRating(organizer.getAverageRating())
                                .totalReviews(organizer.getTotalReviews())
                                .eventsHosted(organizer.getEventsHosted() != null
                                                ? organizer.getEventsHosted().stream().map(Event::getId).toList()
                                                : null)
                                .createdAt(organizer.getCreatedAt())
                                .updatedAt(organizer.getUpdatedAt())
                                .build();
        }
}