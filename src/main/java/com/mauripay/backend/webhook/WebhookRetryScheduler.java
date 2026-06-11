package com.mauripay.backend.webhook;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WebhookRetryScheduler {

    private final WebhookService webhookService;

    public WebhookRetryScheduler(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /** Polls for due webhook deliveries (new + retries) every 15 seconds. */
    @Scheduled(fixedDelayString = "15000")
    public void run() {
        webhookService.dispatchDue();
    }
}
