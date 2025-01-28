package com.creatorfund.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID id;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private ZonedDateTime createdAt;
    private ZonedDateTime processedAt;
    private UUID pledgeId;
    private UUID projectId;
    private String projectTitle;
    private UUID backerId;
    private String backerName;
}
