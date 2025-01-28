package com.creatorfund.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class RefundResponse {
    private UUID id;
    private BigDecimal amount;
    private String reason;
    private String status;
    private ZonedDateTime createdAt;
    private ZonedDateTime processedAt;
    private UUID transactionId;
    private UUID projectId;
    private String projectTitle;
    private UUID backerId;
    private String backerName;
}
