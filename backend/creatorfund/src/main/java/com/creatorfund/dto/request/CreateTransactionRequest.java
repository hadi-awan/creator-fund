package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateTransactionRequest {
    @NotNull(message = "Pledge ID is required")
    private UUID pledgeId;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must not be negative")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter code (e.g., USD)")
    private String currency;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String paymentProviderRef;
}
