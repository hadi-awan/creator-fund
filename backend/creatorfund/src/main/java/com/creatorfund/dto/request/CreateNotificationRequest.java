package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateNotificationRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Content is required")
    private String content;

    private UUID referenceId;
}
