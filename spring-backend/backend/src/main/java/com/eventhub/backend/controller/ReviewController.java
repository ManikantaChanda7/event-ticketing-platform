package com.eventhub.backend.controller;

import com.eventhub.backend.dto.EventRatingResponse;
import com.eventhub.backend.dto.ReviewRequest;
import com.eventhub.backend.dto.ReviewResponse;
import com.eventhub.backend.entity.Review;
import com.eventhub.backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                reviewService.createReview(
                        request,
                        authentication.getName()));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ReviewResponse>> getEventReviews(
            @PathVariable Long eventId) {

        return ResponseEntity.ok(
                reviewService.getEventReviews(eventId));
    }

    @GetMapping("/event/{eventId}/rating")
    public ResponseEntity<EventRatingResponse> getEventRating(
            @PathVariable Long eventId) {

        return ResponseEntity.ok(
                reviewService.getEventRating(eventId));
    }
}