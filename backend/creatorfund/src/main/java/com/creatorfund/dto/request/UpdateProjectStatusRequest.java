package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProjectStatusRequest {
    @NotNull(message = "Status is required")
    private String status;

    private String moderationNote;
}
