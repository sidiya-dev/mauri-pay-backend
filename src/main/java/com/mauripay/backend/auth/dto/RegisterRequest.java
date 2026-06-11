package com.mauripay.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 32) String phone,
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Size(min = 6, max = 72) String password) {
}
