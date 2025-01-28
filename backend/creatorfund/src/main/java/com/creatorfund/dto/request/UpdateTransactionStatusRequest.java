package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateTransactionStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;

    private String paymentProviderRef;
}
