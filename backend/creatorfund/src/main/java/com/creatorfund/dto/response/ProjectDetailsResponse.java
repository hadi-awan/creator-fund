package com.creatorfund.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProjectDetailsResponse {
    private UUID id;
    private String title;
    private String description;
    private String shortDescription;
    private BigDecimal fundingGoal;
    private BigDecimal currentAmount;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private String status;
    private String riskAssessment;
    private ProjectStatsResponse stats;
    private UserSummaryResponse creator;
    private String categoryName;
    private List<RewardTierResponse> rewardTiers;
    private List<MilestoneResponse> milestones;
}
