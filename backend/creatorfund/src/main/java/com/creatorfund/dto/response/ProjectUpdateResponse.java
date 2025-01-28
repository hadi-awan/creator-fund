package com.creatorfund.dto.response;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectUpdateResponse {
    private UUID id;
    private String title;
    private String content;
    private String updateType;
    private ZonedDateTime createdAt;
    private UserSummaryResponse createdBy;
}
