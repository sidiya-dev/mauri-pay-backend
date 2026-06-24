package com.mauripay.backend.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Snake_case keys match what the mobile app's TransactionEntity expects. */
public record TransactionResponse(
        UUID id,
        @JsonProperty("sender_id") UUID senderId,
        @JsonProperty("receiver_id") UUID receiverId,
        BigDecimal amount,
        @JsonProperty("transaction_type") String transactionType,
        @JsonProperty("created_at") Instant createdAt) {

    public static TransactionResponse from(LedgerTransaction t) {
        return new TransactionResponse(t.getId(), t.getSenderId(), t.getReceiverId(),
                t.getAmount(), t.getTransactionType(), t.getCreatedAt());
    }
}
