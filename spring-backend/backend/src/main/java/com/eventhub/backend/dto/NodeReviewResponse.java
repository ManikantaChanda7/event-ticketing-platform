package com.eventhub.backend.dto;

import java.time.LocalDateTime;

public class NodeReviewResponse {

    private Long id;

    private Integer rating;

    private String review;

    private LocalDateTime createdAt;

    private ReviewUserResponse user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ReviewUserResponse getUser() {
        return user;
    }

    public void setUser(ReviewUserResponse user) {
        this.user = user;
    }
}