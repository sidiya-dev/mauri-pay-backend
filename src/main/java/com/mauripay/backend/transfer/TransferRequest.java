package com.mauripay.backend.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferRequest(
        @JsonProperty("receiverPhone") @NotBlank @Size(max = 32) String receiverPhone,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @JsonProperty("transactionType") @Size(max = 32) String transactionType) {
}
