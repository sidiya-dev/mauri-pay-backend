package com.mauripay.backend.webhook;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {

    List<WebhookDelivery> findTop50ByStatusAndNextAttemptAtBeforeOrderByNextAttemptAt(
            WebhookDelivery.Status status, Instant before);
}
