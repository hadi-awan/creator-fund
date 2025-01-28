package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateNotificationRequest {
    @NotNull(message = "Read status is required")
    private boolean readStatus;
}
