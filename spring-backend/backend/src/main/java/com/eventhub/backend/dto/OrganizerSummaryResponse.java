package com.eventhub.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerSummaryResponse {

    private Long _id;

    private Long id;

    private String orgName;

    private String orgEmail;

    private String orgDescription;

    private String organizerProfileImage;

    private String organizerBannerImage;
}