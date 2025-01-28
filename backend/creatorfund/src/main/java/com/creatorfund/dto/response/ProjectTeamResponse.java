package com.creatorfund.dto.response;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectTeamResponse {
    private UUID id;
    private UserSummaryResponse user;
    private String role;
    private String permissions;
    private ZonedDateTime joinedAt;
}
