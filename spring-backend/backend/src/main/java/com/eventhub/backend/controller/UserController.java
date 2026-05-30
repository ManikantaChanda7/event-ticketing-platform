package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ChangePasswordRequest;
import com.eventhub.backend.dto.ProfileResponse;
import com.eventhub.backend.dto.UpdateEmailRequest;
import com.eventhub.backend.dto.UpdateLocationRequest;
import com.eventhub.backend.dto.UpdateProfileRequest;
import com.eventhub.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

        private final UserService userService;

        public UserController(
                        UserService userService) {

                this.userService = userService;
        }

        @GetMapping("/profile")
        public ResponseEntity<ProfileResponse> getProfile(
                        Authentication authentication) {

                return ResponseEntity.ok(
                                userService.getProfile(
                                                authentication.getName()));
        }

        @PutMapping("/profile")
        public ResponseEntity<ProfileResponse> updateProfile(
                        Authentication authentication,
                        @RequestBody UpdateProfileRequest request) {

                return ResponseEntity.ok(
                                userService.updateProfile(
                                                authentication.getName(),
                                                request));
        }

        @PutMapping("/change-password")
        public ResponseEntity<String> changePassword(
                        Authentication authentication,
                        @RequestBody ChangePasswordRequest request) {

                userService.changePassword(
                                authentication.getName(),
                                request);

                return ResponseEntity.ok(
                                "Password changed successfully");
        }

        @PutMapping("/email")
        public ResponseEntity<ProfileResponse> updateEmail(
                        @RequestBody UpdateEmailRequest request) {

                return ResponseEntity.ok(
                                userService.updateEmail(request));
        }

        @PutMapping("/location")
        public ResponseEntity<ProfileResponse> updateLocation(
                        @RequestBody UpdateLocationRequest request) {

                return ResponseEntity.ok(
                                userService.updateLocation(
                                                request));
        }

        @GetMapping("/location")
        public ResponseEntity<UpdateLocationRequest> getLocation() {

                return ResponseEntity.ok(
                                userService.getLocation());
        }
}