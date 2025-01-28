package com.creatorfund.dto.response;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private String type;
    private String content;
    private UUID referenceId;
    private String referenceType;
    private boolean readStatus;
    private ZonedDateTime createdAt;
    private Map<String, Object> additionalData;
}
