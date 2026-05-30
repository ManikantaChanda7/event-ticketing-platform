package com.eventhub.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class EventSummaryResponse {
    private Long _id;
    private Long id;
    private String title;
    private String category;
    private String bannerImage;
    private String status;
    private String thumbnailImage;

    private Double startingPrice;

    private String recurrence;

    private String startDate;

    private String endDate;

    private String startTime;

    private LocationResponse location;

    private Double averageRating;

    private Integer interestedUsers;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}