package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateMilestoneRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Target date is required")
    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;

    @NotNull(message = "Fund release amount is required")
    @Min(value = 0, message = "Fund release amount must not be negative")
    private BigDecimal fundReleaseAmount;
}
