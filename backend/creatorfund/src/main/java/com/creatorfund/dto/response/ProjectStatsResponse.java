package com.creatorfund.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;

@Data
@Builder
public class ProjectStatsResponse {
    private int totalBackers;
    private BigDecimal totalRaised;
    private BigDecimal percentageFunded;
    private long daysToGo;
    private int completedMilestones;
    private int totalMilestones;
    private int updateCount;
    private int commentCount;
    private ZonedDateTime lastUpdateAt;
    private Map<String, Integer> backerTiers;
}
