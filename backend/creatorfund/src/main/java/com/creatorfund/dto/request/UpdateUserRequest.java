package com.creatorfund.dto.request;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    private String bio;

    @Size(max = 512, message = "Profile image URL must not exceed 512 characters")
    private String profileImageUrl;

    @Email(message = "Invalid email format")
    private String email;

    // Note: Password update should be handled by a separate DTO for security
}
