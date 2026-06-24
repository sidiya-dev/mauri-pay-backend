package com.mauripay.backend.merchant.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MerchantProfileResponse(
        UUID merchantId,
        String name,
        String callbackUrl,
        BigDecimal balance,
        String currency) {
}
