package com.mauripay.backend.payment.dto;

import com.mauripay.backend.payment.PaymentStatus;

import java.math.BigDecimal;

/**
 * Read-only view shown to the paying user. The amount is fixed by the merchant
 * and cannot be edited by the user — there is intentionally no amount input on pay.
 */
public record PaymentPreview(
        String code,
        BigDecimal amount,
        String currency,
        String merchantName,
        PaymentStatus status) {
}
