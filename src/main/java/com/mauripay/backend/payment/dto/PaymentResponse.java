package com.mauripay.backend.payment.dto;

import com.mauripay.backend.payment.PaymentRequest;
import com.mauripay.backend.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/** Full view returned to the merchant (includes code + status timestamps). */
public record PaymentResponse(
        String code,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String orderRef,
        Instant expiresAt,
        Instant paidAt) {

    public static PaymentResponse from(PaymentRequest p) {
        return new PaymentResponse(p.getCode(), p.getAmount(), p.getCurrency(), p.getStatus(),
                p.getOrderRef(), p.getExpiresAt(), p.getPaidAt());
    }
}
