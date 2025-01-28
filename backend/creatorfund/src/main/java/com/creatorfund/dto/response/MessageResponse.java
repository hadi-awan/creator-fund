package com.creatorfund.dto.response;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {
    private UUID id;
    private String content;
    private UUID senderId;
    private String senderName;
    private String senderProfileImage;
    private UUID recipientId;
    private String recipientName;
    private UUID projectId;
    private String projectTitle;
    private boolean readStatus;
    private ZonedDateTime createdAt;
}
