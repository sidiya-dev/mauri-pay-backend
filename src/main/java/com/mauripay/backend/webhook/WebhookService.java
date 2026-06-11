package com.mauripay.backend.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mauripay.backend.common.HmacSigner;
import com.mauripay.backend.config.AppProperties;
import com.mauripay.backend.merchant.Merchant;
import com.mauripay.backend.merchant.MerchantRepository;
import com.mauripay.backend.payment.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final WebhookDeliveryRepository deliveryRepository;
    private final MerchantRepository merchantRepository;
    private final WebhookSender sender;
    private final HmacSigner signer;
    private final ObjectMapper objectMapper;
    private final AppProperties properties;

    public WebhookService(WebhookDeliveryRepository deliveryRepository,
                          MerchantRepository merchantRepository, WebhookSender sender,
                          HmacSigner signer, ObjectMapper objectMapper, AppProperties properties) {
        this.deliveryRepository = deliveryRepository;
        this.merchantRepository = merchantRepository;
        this.sender = sender;
        this.signer = signer;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /** Persists a pending delivery for a paid request. No-op if the merchant supplied no callback URL. */
    @Transactional
    public void enqueuePaid(PaymentRequest payment) {
        if (payment.getCallbackUrl() == null || payment.getCallbackUrl().isBlank()) {
            return;
        }
        Merchant merchant = merchantRepository.findById(payment.getMerchantId()).orElseThrow();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", payment.getCode());
        body.put("status", payment.getStatus().name());
        body.put("amount", payment.getAmount());
        body.put("currency", payment.getCurrency());
        body.put("orderRef", payment.getOrderRef());
        body.put("paidAt", payment.getPaidAt());

        String payload = serialize(body);
        String signature = signer.sign(merchant.getWebhookSecret(), payload);
        deliveryRepository.save(
                new WebhookDelivery(payment.getId(), payment.getCallbackUrl(), payload, signature));
    }

    /** Attempts every due delivery, recording success/failure with exponential backoff. */
    @Transactional
    public void dispatchDue() {
        var due = deliveryRepository.findTop50ByStatusAndNextAttemptAtBeforeOrderByNextAttemptAt(
                WebhookDelivery.Status.PENDING, Instant.now());
        for (WebhookDelivery delivery : due) {
            attempt(delivery);
        }
    }

    private void attempt(WebhookDelivery delivery) {
        try {
            sender.post(delivery.getUrl(), delivery.getPayload(), delivery.getSignature());
            delivery.markDelivered();
            log.info("Webhook delivered to {} (attempt {})", delivery.getUrl(), delivery.getAttempts());
        } catch (Exception ex) {
            int nextAttemptNumber = delivery.getAttempts() + 1;
            boolean giveUp = nextAttemptNumber >= properties.webhook().maxAttempts();
            long backoff = (long) properties.webhook().retryBaseSeconds() * (1L << delivery.getAttempts());
            delivery.recordFailure(ex.getMessage(),
                    Instant.now().plus(backoff, ChronoUnit.SECONDS), giveUp);
            log.warn("Webhook to {} failed (attempt {}, giveUp={}): {}",
                    delivery.getUrl(), nextAttemptNumber, giveUp, ex.getMessage());
        }
    }

    private String serialize(Map<String, Object> body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize webhook payload", e);
        }
    }
}
