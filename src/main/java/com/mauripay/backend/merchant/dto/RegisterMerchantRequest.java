package com.mauripay.backend.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record RegisterMerchantRequest(
        @NotBlank @Size(max = 120) String name,
        @URL @Size(max = 500) String callbackUrl) {
}
