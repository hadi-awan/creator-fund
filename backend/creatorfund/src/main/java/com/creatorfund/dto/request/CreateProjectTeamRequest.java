package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateProjectTeamRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotBlank(message = "Role is required")
    private String role;

    private String permissions;
}
