package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import com.eventhub.backend.dto.LoginRequest;
import com.eventhub.backend.dto.LoginResponse;
import com.eventhub.backend.dto.ProfileResponse;
import com.eventhub.backend.dto.RegisterRequest;
import com.eventhub.backend.dto.RegisterResponse;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(request);
        // TODO: Populate token, userId, userRole, interestedEvents properly
        RegisterResponse response = new RegisterResponse(null, user.getId(), user.getRole().name(), null);
        return new ApiResponse<>(true, "User registered successfully", response);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        String token = userService.loginUser(
                request.getEmail(),
                request.getPassword());

        // Fetch user to populate response fields
        ProfileResponse profile = userService.getProfile(request.getEmail());

        // Fetch user's interested events
        List<Long> userInterests = userService.getUserInterestedEventIds(request.getEmail());

        LoginResponse response = new LoginResponse(token, profile.getId(), profile.getRole(), userInterests);
        return new ApiResponse<>(true, "Login successful", response);
    }
}