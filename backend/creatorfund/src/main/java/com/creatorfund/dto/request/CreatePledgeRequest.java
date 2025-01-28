package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreatePledgeRequest {

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    private UUID rewardTierId;

    private boolean anonymous;
}

