package com.mauripay.backend.payment.dto;

import jakarta.validation.constraints.NotBlank;

/** The user re-enters their account password to authorize the payment. No amount is accepted. */
public record PayRequest(@NotBlank String password) {
}
