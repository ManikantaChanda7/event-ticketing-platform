package com.eventhub.backend.service;

import com.eventhub.backend.dto.ChangePasswordRequest;
import com.eventhub.backend.dto.LoginDataResponse;
import com.eventhub.backend.dto.ProfileResponse;
import com.eventhub.backend.dto.RegisterRequest;
import com.eventhub.backend.dto.UpdateEmailRequest;
import com.eventhub.backend.dto.UpdateLocationRequest;
import com.eventhub.backend.dto.UpdateProfileRequest;
import com.eventhub.backend.entity.User;

import java.util.List;

public interface UserService {

        User registerUser(RegisterRequest request);

        String loginUser(String email, String password);

        String generateRefreshToken(String email);

        String refreshAccessToken(String refreshToken);

        ProfileResponse getProfile(

                        String userEmail);

        ProfileResponse updateProfile(

                        String userEmail,

                        UpdateProfileRequest request);

        void changePassword(
                        String userEmail,
                        ChangePasswordRequest request);

        ProfileResponse updateEmail(
                        UpdateEmailRequest request);

        ProfileResponse updateLocation(
                        UpdateLocationRequest request);

        UpdateLocationRequest getLocation();

        List<Long> getUserInterestedEventIds(String userEmail);

        ProfileResponse updateInterests(Long userId);

        LoginDataResponse getLoginData(String email, String password);
}