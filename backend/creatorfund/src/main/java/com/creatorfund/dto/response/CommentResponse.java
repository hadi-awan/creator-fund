package com.creatorfund.dto.response;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CommentResponse {
    private UUID id;
    private String content;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String status;
    private UserSummaryResponse user;
    private List<CommentResponse> replies;
}
