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

    private String organizationName;

    private Boolean verified;

    private String website;
}