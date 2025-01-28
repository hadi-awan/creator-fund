package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class UpdateMilestoneStatusRequest {
    @NotNull(message = "Status is required")
    private String status;

    // Optional completion date, required when status is COMPLETED
    private ZonedDateTime completionDate;

    private String statusNote;
}
