package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateMessageRequest {
    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 1000, message = "Message must be between 1 and 1000 characters")
    private String content;

    @NotNull(message = "Recipient ID is required")
    private UUID recipientId;

    private UUID projectId; // Optional, only if message is related to a project
}
