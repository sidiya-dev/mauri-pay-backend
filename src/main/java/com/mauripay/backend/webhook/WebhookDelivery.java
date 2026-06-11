package com.mauripay.backend.webhook;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_delivery")
public class WebhookDelivery {

    public enum Status {
        PENDING,
        DELIVERED,
        FAILED
    }

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "payment_request_id", nullable = false)
    private UUID paymentRequestId;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private String signature;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt = Instant.now();

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected WebhookDelivery() {
    }

    public WebhookDelivery(UUID paymentRequestId, String url, String payload, String signature) {
        this.paymentRequestId = paymentRequestId;
        this.url = url;
        this.payload = payload;
        this.signature = signature;
    }

    public void markDelivered() {
        this.status = Status.DELIVERED;
        this.attempts++;
        this.lastError = null;
    }

    public void recordFailure(String error, Instant nextAttemptAt, boolean giveUp) {
        this.attempts++;
        this.lastError = error;
        this.nextAttemptAt = nextAttemptAt;
        this.status = giveUp ? Status.FAILED : Status.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getPayload() {
        return payload;
    }

    public String getSignature() {
        return signature;
    }

    public int getAttempts() {
        return attempts;
    }

    public Status getStatus() {
        return status;
    }
}
