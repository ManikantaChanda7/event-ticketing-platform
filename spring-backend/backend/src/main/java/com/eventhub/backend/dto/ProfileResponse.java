package com.eventhub.backend.dto;

import java.util.List;

public class ProfileResponse {

    private Long _id;
    private Long id;
    private String username;
    private String email;
    private String role;
    private String phone;
    private String userProfileImage;
    private Boolean isOAuth;
    private LocationResponse preferredLocation;
    private List<Long> interestedEvents;

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public Boolean getIsOAuth() {
        return isOAuth;
    }

    public void setIsOAuth(Boolean isOAuth) {
        this.isOAuth = isOAuth;
    }

    public LocationResponse getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(LocationResponse preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public List<Long> getInterestedEvents() {
        return interestedEvents;
    }

    public void setInterestedEvents(List<Long> interestedEvents) {
        this.interestedEvents = interestedEvents;
    }
}