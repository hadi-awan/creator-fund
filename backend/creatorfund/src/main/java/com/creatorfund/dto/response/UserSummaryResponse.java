package com.creatorfund.dto.response;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class UserSummaryResponse {
    private UUID id;
    private String fullName;
    private String profileImageUrl;
    private String location;
    private ZonedDateTime createdAt;
    private int projectsCreated;
    private int projectsBacked;
}
