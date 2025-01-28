package com.creatorfund.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class RewardTierResponse {
    private UUID id;
    private String title;
    private String description;
    private BigDecimal amount;
    private Integer limitCount;
    private Integer currentBackers;
    private LocalDate estimatedDeliveryDate;
    private String shippingType;
    private boolean isAvailable;
}
