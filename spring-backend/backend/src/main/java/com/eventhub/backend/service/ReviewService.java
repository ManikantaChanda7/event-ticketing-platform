package com.eventhub.backend.service;

import com.eventhub.backend.dto.EventRatingResponse;
import com.eventhub.backend.dto.ReviewRequest;
import com.eventhub.backend.dto.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse createReview(
        ReviewRequest request,
        String userEmail);

    List<ReviewResponse> getEventReviews(Long eventId);

    EventRatingResponse getEventRating(Long eventId);
}