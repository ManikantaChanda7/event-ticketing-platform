package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.ChangePasswordRequest;
import com.eventhub.backend.dto.LocationResponse;
import com.eventhub.backend.dto.LoginDataResponse;
import com.eventhub.backend.dto.ProfileResponse;
import com.eventhub.backend.dto.RegisterRequest;
import com.eventhub.backend.dto.UpdateEmailRequest;
import com.eventhub.backend.dto.UpdateLocationRequest;
import com.eventhub.backend.dto.UpdateProfileRequest;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.ResourceNotFoundException;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.service.UserService;
import com.eventhub.backend.util.JwtService;

import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;

        private final JwtService jwtService;

        private final PasswordEncoder passwordEncoder;

        public UserServiceImpl(
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {

                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtService = jwtService;
        }

        @Override
        public User registerUser(RegisterRequest request) {

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("Email already exists");
                }

                User user = new User();

                user.setUsername(request.getUsername());
                user.setEmail(request.getEmail());
                user.setPassword(
                                passwordEncoder.encode(request.getPassword()));
                user.setRole(Role.USER);

                return userRepository.save(user);
        }

        @Override
        public String loginUser(String email, String password) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

                if (!passwordEncoder.matches(password, user.getPassword())) {
                        throw new RuntimeException("Invalid credentials");
                }

                return jwtService.generateToken(user.getEmail());
        }

        @Override
        public String generateRefreshToken(String email) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                return jwtService.generateRefreshToken(user.getEmail());
        }

        @Override
        public String refreshAccessToken(String refreshToken) {

                String email = jwtService.extractUsername(refreshToken);

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (!jwtService.isTokenValid(refreshToken, user.getEmail())) {
                        throw new RuntimeException("Invalid refresh token");
                }

                // Validate that the token is actually a refresh token
                // For backward compatibility, allow old tokens without type claim
                String tokenType = jwtService.extractTokenType(refreshToken);
                if (tokenType != null && !"refresh".equals(tokenType)) {
                        throw new RuntimeException("Invalid token type - expected refresh token");
                }

                return jwtService.generateToken(user.getEmail());
        }

        @Override

        public ProfileResponse getProfile(

                        String userEmail) {

                User user = userRepository.findByEmail(userEmail)

                                .orElseThrow(() ->

                                new ResourceNotFoundException(

                                                "User not found"));

                return mapToProfileResponse(user);

        }

        @Override
        @Transactional
        public ProfileResponse updateProfile(

                        String userEmail,

                        UpdateProfileRequest request) {

                User user = userRepository.findByEmail(userEmail)

                                .orElseThrow(() ->

                                new ResourceNotFoundException(

                                                "User not found"));

                if (request.getFirstName() != null || request.getLastName() != null) {
                        String firstName = request.getFirstName() != null ? request.getFirstName() : "";
                        String lastName = request.getLastName() != null ? request.getLastName() : "";
                        user.setUsername(firstName + " " + lastName);
                }

                if (request.getPhone() != null) {

                        user.setPhone(request.getPhone());

                }

                if (request.getUserProfileImage() != null) {

                        user.setUserProfileImage(

                                        request.getUserProfileImage());

                }

                user = userRepository.saveAndFlush(user);

                return mapToProfileResponse(user);

        }

        private ProfileResponse mapToProfileResponse(

                        User user) {
                return mapToProfileResponse(user, null);
        }

        private ProfileResponse mapToProfileResponse(
                        User user,
                        List<Long> preloadedInterestedEventIds) {

                ProfileResponse response =

                                new ProfileResponse();

                response.set_id(user.getId());
                response.setId(user.getId());

                response.setUsername(user.getUsername());

                // Split username into firstName and lastName
                String[] nameParts = user.getUsername() != null ? user.getUsername().split(" ", 2)
                                : new String[] { "", "" };
                response.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
                response.setLastName(nameParts.length > 1 ? nameParts[1] : "");

                response.setEmail(user.getEmail());

                response.setRole(

                                user.getRole().name());

                response.setPhone(user.getPhone());

                response.setUserProfileImage(

                                user.getUserProfileImage());

                response.setIsOAuth(user.getIsOAuth());

                // Map preferred location
                if (user.getPreferredLocationLatitude() != null
                                && user.getPreferredLocationLongitude() != null) {
                        LocationResponse location = new LocationResponse();
                        location.setType(user.getPreferredLocationType());
                        location.setLabel(user.getPreferredLocationLabel());
                        location.setCoordinates(List.of(
                                        user.getPreferredLocationLongitude(),
                                        user.getPreferredLocationLatitude()));
                        response.setPreferredLocation(location);
                }

                // Map interested events - use pre-loaded if available, otherwise fetch
                List<Long> interestedEventIds = preloadedInterestedEventIds;
                if (interestedEventIds == null) {
                        interestedEventIds = getUserInterestedEventIds(user.getEmail());
                }
                response.setInterestedEvents(interestedEventIds);

                return response;

        }

        @Override
        public void changePassword(
                        String userEmail,
                        ChangePasswordRequest request) {

                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                if (!passwordEncoder.matches(
                                request.getCurrentPassword(),
                                user.getPassword())) {

                        throw new RuntimeException(
                                        "Current password is incorrect");
                }

                user.setPassword(
                                passwordEncoder.encode(
                                                request.getNewPassword()));

                userRepository.save(user);
        }

        @Override
        public ProfileResponse updateEmail(
                        UpdateEmailRequest request) {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                if (request.getNewEmail() == null
                                || request.getConfirmEmail() == null) {

                        throw new RuntimeException(
                                        "Both email fields are required");
                }

                if (!request.getNewEmail()
                                .equals(request.getConfirmEmail())) {

                        throw new RuntimeException(
                                        "Emails do not match");
                }

                if (userRepository
                                .findByEmail(request.getNewEmail())
                                .isPresent()) {

                        throw new RuntimeException(
                                        "Email already in use");
                }

                user.setEmail(request.getNewEmail());

                return mapToProfileResponse(
                                userRepository.save(user));
        }

        @Override
        public ProfileResponse updateLocation(
                        UpdateLocationRequest request) {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                user.setPreferredLocationLatitude(
                                request.getLatitude());

                user.setPreferredLocationLongitude(
                                request.getLongitude());

                user.setPreferredLocationLabel(
                                request.getLocationLabel());

                return mapToProfileResponse(
                                userRepository.save(user));
        }

        @Override
        public UpdateLocationRequest getLocation() {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                UpdateLocationRequest response = new UpdateLocationRequest();

                response.setLatitude(
                                user.getPreferredLocationLatitude());

                response.setLongitude(
                                user.getPreferredLocationLongitude());

                response.setLocationLabel(
                                user.getPreferredLocationLabel());

                return response;
        }

        @Override
        public List<Long> getUserInterestedEventIds(String userEmail) {
                User user = userRepository
                                .findByEmailWithInterests(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                return user.getInterestedEvents()
                                .stream()
                                .map(event -> event.getId())
                                .toList();
        }

        @Override
        public ProfileResponse updateInterests(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));
                return mapToProfileResponse(user);
        }

        @Override
        public LoginDataResponse getLoginData(String email, String password) {
                // Fetch user with interested events in a single query
                User user = userRepository.findUserWithInterestsForLogin(email)
                                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

                // Validate password
                if (!passwordEncoder.matches(password, user.getPassword())) {
                        throw new RuntimeException("Invalid credentials");
                }

                // Generate tokens
                String accessToken = jwtService.generateToken(user.getEmail());
                String refreshToken = jwtService.generateRefreshToken(user.getEmail());

                // Extract interested event IDs from the already-loaded collection
                List<Long> userInterests = user.getInterestedEvents()
                                .stream()
                                .map(event -> event.getId())
                                .toList();

                // Map to profile response using pre-loaded interested events to avoid redundant query
                ProfileResponse profile = mapToProfileResponse(user, userInterests);

                return new LoginDataResponse(accessToken, refreshToken, profile, userInterests);
        }
}