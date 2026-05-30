package com.eventhub.backend.controller;

import com.eventhub.backend.dto.LoginRequest;
import com.eventhub.backend.dto.LoginResponse;
import com.eventhub.backend.dto.RegisterRequest;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)

    public User register(@Valid @RequestBody RegisterRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        String token = userService.loginUser(
                request.getEmail(),
                request.getPassword());

        return new LoginResponse(token);
    }
}