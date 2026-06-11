package com.mauripay.backend.auth.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String phone,
        String fullName,
        BigDecimal balance,
        String currency) {
}
