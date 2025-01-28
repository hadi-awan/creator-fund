package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateRefundRequest {
    @NotNull(message = "Transaction ID is required")
    private UUID transactionId;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must not be negative")
    private BigDecimal amount;

    @NotBlank(message = "Reason is required")
    private String reason;
}
