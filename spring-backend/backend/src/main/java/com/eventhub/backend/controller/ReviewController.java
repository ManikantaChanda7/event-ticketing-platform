package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import com.eventhub.backend.dto.EventRatingResponse;
import com.eventhub.backend.dto.ReviewRequest;
import com.eventhub.backend.dto.ReviewResponse;
import com.eventhub.backend.entity.Review;
import com.eventhub.backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

        private final ReviewService reviewService;

        public ReviewController(
                        ReviewService reviewService) {

                this.reviewService = reviewService;
        }

        @PostMapping
        public ApiResponse<ReviewResponse> createReview(
                        @Valid @RequestBody ReviewRequest request,
                        Authentication authentication) {
                ReviewResponse response = reviewService.createReview(request, authentication.getName());
                return new ApiResponse<>(true, "Review created successfully", response);
        }

        @GetMapping("/event/{eventId}")
        public ApiResponse<List<ReviewResponse>> getEventReviews(
                        @PathVariable Long eventId) {
                List<ReviewResponse> response = reviewService.getEventReviews(eventId);
                return new ApiResponse<>(true, "Event reviews fetched successfully", response);
        }

        @GetMapping("/event/{eventId}/rating")
        public ApiResponse<EventRatingResponse> getEventRating(
                        @PathVariable Long eventId) {
                EventRatingResponse response = reviewService.getEventRating(eventId);
                return new ApiResponse<>(true, "Event rating fetched successfully", response);
        }
}