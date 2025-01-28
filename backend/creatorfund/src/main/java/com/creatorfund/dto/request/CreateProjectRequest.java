package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CreateProjectRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 20, message = "Description must be at least 20 characters")
    private String description;

    @Size(max = 512, message = "Short description must not exceed 512 characters")
    private String shortDescription;

    @NotNull(message = "Funding goal is required")
    @Min(value = 1, message = "Funding goal must be greater than 0")
    private BigDecimal fundingGoal;

    @NotNull(message = "Category is required")
    private UUID categoryId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private ZonedDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private ZonedDateTime endDate;

    private String riskAssessment;

    private List<CreateRewardTierRequest> rewardTiers;

    private List<CreateMilestoneRequest> milestones;
}
