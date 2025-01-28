package com.creatorfund.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
public class UserStatsResponse {
    private int projectsCreated;
    private int projectsBacked;
    private BigDecimal totalFunded;
    private BigDecimal totalRaised;
    private int successfulProjects;
    private int activeProjects;
    private double successRate;
    private int totalUpdatesPosted;
    private int followersCount;
    private int followingCount;
}
