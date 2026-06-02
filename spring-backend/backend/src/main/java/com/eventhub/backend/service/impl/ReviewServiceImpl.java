package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.EventRatingResponse;
import com.eventhub.backend.dto.NodeReviewResponse;
import com.eventhub.backend.dto.ReviewRequest;
import com.eventhub.backend.dto.ReviewResponse;
import com.eventhub.backend.dto.ReviewUserResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Review;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.exception.ResourceNotFoundException;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.ReviewRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

        private final ReviewRepository reviewRepository;
        private final UserRepository userRepository;
        private final EventRepository eventRepository;

        public ReviewServiceImpl(
                        ReviewRepository reviewRepository,
                        UserRepository userRepository,
                        EventRepository eventRepository) {

                this.reviewRepository = reviewRepository;
                this.userRepository = userRepository;
                this.eventRepository = eventRepository;
        }

        @Override
        public ReviewResponse createReview(
                        ReviewRequest request,
                        String userEmail) {

                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Event event = eventRepository.findById(request.getEventId())
                                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

                reviewRepository.findByUserAndEvent(user, event)
                                .ifPresent(review -> {
                                        throw new RuntimeException(
                                                        "You have already reviewed this event");
                                });

                Review review = new Review();
                review.setUser(user);
                review.setEvent(event);
                review.setRating(request.getRating());
                review.setReview(request.getComment());

                Review savedReview = reviewRepository.save(review);

                Double averageRating = reviewRepository.findAverageRatingByEvent(event);

                event.setAverageRating(
                                averageRating == null ? 0.0 : averageRating);

                eventRepository.save(event);

                return mapToResponse(savedReview);
        }

        @Override
        public List<NodeReviewResponse> getEventReviews(Long eventId) {

                Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

                return reviewRepository.findByEvent(event).stream()
                                .map(this::mapToNodeResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public EventRatingResponse getEventRating(Long eventId) {

                Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

                Double averageRating = reviewRepository.findAverageRatingByEvent(event);

                Long totalReviews = reviewRepository.countByEvent(event);

                EventRatingResponse response = new EventRatingResponse();

                response.setAverageRating(
                                averageRating == null ? 0.0 : averageRating);

                response.setTotalReviews(totalReviews);

                return response;
        }

        private ReviewResponse mapToResponse(
                        Review review) {

                ReviewResponse response = new ReviewResponse();

                response.setId(review.getId());
                response.setUsername(
                                review.getUser().getUsername());

                response.setRating(
                                review.getRating());

                response.setComment(
                                review.getReview());

                return response;
        }

        private NodeReviewResponse mapToNodeResponse(
                        Review review) {

                ReviewUserResponse userResponse = new ReviewUserResponse(
                                review.getUser().getId(),
                                review.getUser().getUsername(),
                                review.getUser().getUserProfileImage());

                NodeReviewResponse response = new NodeReviewResponse();
                response.setId(review.getId());
                response.setRating(review.getRating());
                response.setReview(review.getReview());
                response.setCreatedAt(review.getCreatedAt());
                response.setUser(userResponse);

                return response;
        }
}