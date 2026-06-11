package com.mauripay.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Payment payment, Webhook webhook, Dev dev) {

    public record Payment(int codeTtlMinutes) {
    }

    public record Webhook(int maxAttempts, int retryBaseSeconds) {
    }

    public record Dev(boolean seed, String seedMerchantApiKey) {
    }
}
