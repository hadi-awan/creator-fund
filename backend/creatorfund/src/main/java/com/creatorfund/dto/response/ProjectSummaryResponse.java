package com.creatorfund.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectSummaryResponse {
    private UUID id;
    private String title;
    private String shortDescription;
    private BigDecimal fundingGoal;
    private BigDecimal currentAmount;
    private double percentageFunded;
    private ZonedDateTime endDate;
    private long daysToGo;
    private String status;
    private UserSummaryResponse creator;
    private String categoryName;
    private int totalBackers;
}
