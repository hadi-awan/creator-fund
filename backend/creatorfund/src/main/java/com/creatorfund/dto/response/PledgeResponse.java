package com.creatorfund.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class PledgeResponse {
    private UUID id;
    private BigDecimal amount;
    private String status;
    private ZonedDateTime createdAt;
    private UserSummaryResponse backer;
    private RewardTierResponse rewardTier;
    private boolean anonymous;
}
