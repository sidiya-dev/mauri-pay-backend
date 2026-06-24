package com.mauripay.backend.merchant.dto;

import java.util.UUID;

/**
 * Returned once at registration. The {@code apiKey} and {@code webhookSecret} are shown a single
 * time and cannot be retrieved later (only their hash/usage is kept server-side).
 */
public record MerchantRegisteredResponse(
        UUID merchantId,
        String name,
        String apiKey,
        String webhookSecret,
        String callbackUrl) {
}
