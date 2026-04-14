package com.example.lucidplus_onboarding.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    @NotBlank(message = "Mobile is required")
    private String mobile;

    @NotBlank(message = "OTP is required")
    private String otp;
}
