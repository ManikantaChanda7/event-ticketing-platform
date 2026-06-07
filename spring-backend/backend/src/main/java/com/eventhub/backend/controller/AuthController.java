package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import com.eventhub.backend.dto.LoginRequest;
import com.eventhub.backend.dto.LoginResponse;
import com.eventhub.backend.dto.OrganizerRequest;
import com.eventhub.backend.dto.OrganizerResponse;
import com.eventhub.backend.dto.ProfileResponse;
import com.eventhub.backend.dto.RegisterRequest;
import com.eventhub.backend.dto.RegisterResponse;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.service.OrganizerService;
import com.eventhub.backend.service.UserService;
import com.eventhub.backend.util.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final UserService userService;
        private final JwtService jwtService;
        private final OrganizerService organizerService;

        public AuthController(UserService userService, JwtService jwtService, OrganizerService organizerService) {
                this.userService = userService;
                this.jwtService = jwtService;
                this.organizerService = organizerService;
        }

        @PostMapping("/register")
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
                User user = userService.registerUser(request);
                String token = userService.loginUser(
                                request.getEmail(),
                                request.getPassword());
                String refreshToken = userService.generateRefreshToken(
                                request.getEmail());

                RegisterResponse response = new RegisterResponse(
                                token,
                                refreshToken,
                                user.getId(),
                                user.getRole().name(),
                                List.of());
                return new ApiResponse<>(true, "User registered successfully", response);
        }

        @PostMapping("/register-organizer")
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<OrganizerResponse> registerOrganizer(@Valid @RequestBody OrganizerRequest request) {
                OrganizerResponse response = organizerService.createOrganizer(request);
                return new ApiResponse<>(true, "Organizer registered successfully", response);
        }

        @PostMapping("/login")
        public ApiResponse<LoginResponse> login(
                        @Valid @RequestBody LoginRequest request) {

                String accessToken = userService.loginUser(
                                request.getEmail(),
                                request.getPassword());
                String refreshToken = userService.generateRefreshToken(
                                request.getEmail());

                // Fetch user to populate response fields
                ProfileResponse profile = userService.getProfile(request.getEmail());

                // Fetch user's interested events
                List<Long> userInterests = userService.getUserInterestedEventIds(request.getEmail());

                LoginResponse response = new LoginResponse(
                                accessToken,
                                refreshToken,
                                profile.getId(),
                                profile.getRole(),
                                userInterests);
                return new ApiResponse<>(true, "Login successful", response);
        }

        @PostMapping("/refresh")
        public ApiResponse<Map<String, String>> refreshToken(
                        @RequestBody Map<String, String> request) {

                String refreshToken = request.get("refreshToken");

                String email = userService.refreshAccessToken(refreshToken);
                String newAccessToken = jwtService.generateToken(email);
                String newRefreshToken = jwtService.generateRefreshToken(email);

                return new ApiResponse<>(
                                true,
                                "Token refreshed successfully",
                                Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken));
        }
}