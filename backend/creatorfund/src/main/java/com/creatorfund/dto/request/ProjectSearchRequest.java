package com.creatorfund.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ProjectSearchRequest {
    private String query;
    private UUID categoryId;
    private String status;
    private BigDecimal minFundingGoal;
    private BigDecimal maxFundingGoal;
    private LocalDate startDateFrom;
    private LocalDate startDateTo;
    private Boolean hasRewardTiers;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
