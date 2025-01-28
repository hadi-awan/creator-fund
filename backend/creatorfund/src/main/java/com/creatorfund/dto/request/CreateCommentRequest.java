package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateCommentRequest {
    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 1000, message = "Content must be between 1 and 1000 characters")
    private String content;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    private UUID parentCommentId;
}
