package com.eventhub.backend.dto;

import java.util.List;

public class LoginDataResponse {
    private String accessToken;
    private String refreshToken;
    private ProfileResponse profile;
    private List<Long> userInterests;

    public LoginDataResponse() {}

    public LoginDataResponse(String accessToken, String refreshToken, ProfileResponse profile, List<Long> userInterests) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.profile = profile;
        this.userInterests = userInterests;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public ProfileResponse getProfile() {
        return profile;
    }

    public void setProfile(ProfileResponse profile) {
        this.profile = profile;
    }

    public List<Long> getUserInterests() {
        return userInterests;
    }

    public void setUserInterests(List<Long> userInterests) {
        this.userInterests = userInterests;
    }
}
