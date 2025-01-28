package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class UpdateProjectRequest {

    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @Size(min = 20, message = "Description must be at least 20 characters")
    private String description;

    @Size(max = 512, message = "Short description must not exceed 512 characters")
    private String shortDescription;

    private String riskAssessment;

    private UUID categoryId;

    private ZonedDateTime endDate;
}
