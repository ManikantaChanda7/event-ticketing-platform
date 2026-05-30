package com.eventhub.backend.dto;

public class ReviewUserResponse {

    private Long id;

    private String username;

    private String userProfileImage;

    public ReviewUserResponse() {
    }

    public ReviewUserResponse(
            Long id,
            String username,
            String userProfileImage) {

        this.id = id;
        this.username = username;
        this.userProfileImage = userProfileImage;
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

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }
}