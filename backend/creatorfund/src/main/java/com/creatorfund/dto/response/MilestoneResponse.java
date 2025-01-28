package com.creatorfund.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class MilestoneResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDate targetDate;
    private LocalDate completionDate;
    private String status;
    private BigDecimal fundReleaseAmount;
}
