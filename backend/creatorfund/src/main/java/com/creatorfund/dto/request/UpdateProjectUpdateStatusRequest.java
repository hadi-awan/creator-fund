package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProjectUpdateStatusRequest {
    @NotNull(message = "Status is required")
    private String status;

    private String moderationNote;
}
