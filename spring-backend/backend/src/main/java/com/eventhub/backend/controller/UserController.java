package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import com.eventhub.backend.dto.ChangePasswordRequest;
import com.eventhub.backend.dto.ProfileResponse;
import com.eventhub.backend.dto.UpdateEmailRequest;
import com.eventhub.backend.dto.UpdateLocationRequest;
import com.eventhub.backend.dto.UpdateProfileRequest;
import com.eventhub.backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

        private final UserService userService;

        public UserController(
                        UserService userService) {

                this.userService = userService;
        }

        @GetMapping("/profile")
        public ApiResponse<ProfileResponse> getProfile(
                        Authentication authentication) {
                ProfileResponse response = userService.getProfile(authentication.getName());
                return new ApiResponse<>(true, "Profile fetched successfully", response);
        }

        @PutMapping("/profile")
        public ApiResponse<ProfileResponse> updateProfile(
                        Authentication authentication,
                        @RequestBody UpdateProfileRequest request) {
                ProfileResponse response = userService.updateProfile(authentication.getName(), request);
                return new ApiResponse<>(true, "Profile updated successfully", response);
        }

        @PutMapping("/change-password")
        public ApiResponse<Void> changePassword(
                        Authentication authentication,
                        @RequestBody ChangePasswordRequest request) {
                userService.changePassword(authentication.getName(), request);
                return new ApiResponse<>(true, "Password changed successfully", null);
        }

        @PutMapping("/email")
        public ApiResponse<ProfileResponse> updateEmail(
                        @RequestBody UpdateEmailRequest request) {
                ProfileResponse response = userService.updateEmail(request);
                return new ApiResponse<>(true, "Email updated successfully", response);
        }

        @PutMapping("/location")
        public ApiResponse<ProfileResponse> updateLocation(
                        @RequestBody UpdateLocationRequest request) {
                ProfileResponse response = userService.updateLocation(request);
                return new ApiResponse<>(true, "Location updated successfully", response);
        }

        @GetMapping("/location")
        public ApiResponse<UpdateLocationRequest> getLocation() {
                UpdateLocationRequest response = userService.getLocation();
                return new ApiResponse<>(true, "Location fetched successfully", response);
        }

        @GetMapping("/selectedLocation")
        public ApiResponse<UpdateLocationRequest> getSelectedLocation(Authentication authentication) {
                UpdateLocationRequest response = userService.getLocation();
                return new ApiResponse<>(true, "Location fetched successfully", response);
        }

        @PutMapping("/{id}/updateInterests")
        public ApiResponse<ProfileResponse> updateInterests(@PathVariable Long id) {
                ProfileResponse response = userService.updateInterests(id);
                return new ApiResponse<>(true, "Interests updated successfully", response);
        }
}