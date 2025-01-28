package com.creatorfund.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateProjectUpdateRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Update type is required")
    private String updateType;
}
