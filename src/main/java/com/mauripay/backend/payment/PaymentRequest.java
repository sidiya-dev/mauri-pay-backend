package com.mauripay.backend.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_request")
public class PaymentRequest {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, updatable = false)
    private String code;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(nullable = false, updatable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "MRU";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "order_ref")
    private String orderRef;

    @Column(name = "callback_url")
    private String callbackUrl;

    @Column(name = "paid_by_user_id")
    private UUID paidByUserId;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected PaymentRequest() {
    }

    public PaymentRequest(String code, UUID merchantId, BigDecimal amount, String currency,
                          String orderRef, String callbackUrl, Instant expiresAt) {
        this.code = code;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.orderRef = orderRef;
        this.callbackUrl = callbackUrl;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public void markPaid(UUID userId) {
        this.status = PaymentStatus.PAID;
        this.paidByUserId = userId;
        this.paidAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getOrderRef() {
        return orderRef;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public UUID getPaidByUserId() {
        return paidByUserId;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
