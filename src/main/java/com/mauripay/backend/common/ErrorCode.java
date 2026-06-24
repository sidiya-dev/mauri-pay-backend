package com.mauripay.backend.common;

/**
 * Stable machine-readable error codes returned in {@code ErrorResponse.code}.
 * Clients map these to localized (translated) messages — never parse the human message.
 */
public final class ErrorCode {

    private ErrorCode() {
    }

    // Auth
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String INVALID_PASSWORD = "INVALID_PASSWORD";
    public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String NOT_AUTHENTICATED = "NOT_AUTHENTICATED";
    public static final String PHONE_ALREADY_EXISTS = "PHONE_ALREADY_EXISTS";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";
    public static final String MERCHANT_NOT_FOUND = "MERCHANT_NOT_FOUND";

    // Transfer / payment
    public static final String RECIPIENT_NOT_FOUND = "RECIPIENT_NOT_FOUND";
    public static final String SELF_TRANSFER = "SELF_TRANSFER";
    public static final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
    public static final String PAYMENT_NOT_FOUND = "PAYMENT_NOT_FOUND";
    public static final String PAYMENT_ALREADY_PAID = "PAYMENT_ALREADY_PAID";
    public static final String PAYMENT_NOT_PAYABLE = "PAYMENT_NOT_PAYABLE";
    public static final String PAYMENT_EXPIRED = "PAYMENT_EXPIRED";

    // Request / system
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String UNEXPECTED_ERROR = "UNEXPECTED_ERROR";
}
