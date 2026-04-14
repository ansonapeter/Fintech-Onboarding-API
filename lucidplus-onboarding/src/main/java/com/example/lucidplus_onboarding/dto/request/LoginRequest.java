package com.example.lucidplus_onboarding.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Mobile or email is required")
    private String identifier;  // mobile or email


}
