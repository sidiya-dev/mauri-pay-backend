package com.mauripay.backend.ledger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_transaction")
public class LedgerTransaction {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "receiver_id")
    private UUID receiverId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "payment_request_id")
    private UUID paymentRequestId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected LedgerTransaction() {
    }

    public LedgerTransaction(UUID senderId, UUID receiverId, BigDecimal amount,
                             String transactionType, UUID paymentRequestId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.paymentRequestId = paymentRequestId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
