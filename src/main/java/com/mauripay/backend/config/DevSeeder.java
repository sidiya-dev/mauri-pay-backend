package com.mauripay.backend.config;

import com.mauripay.backend.merchant.ApiKeyService;
import com.mauripay.backend.merchant.Merchant;
import com.mauripay.backend.merchant.MerchantAccount;
import com.mauripay.backend.merchant.MerchantAccountRepository;
import com.mauripay.backend.merchant.MerchantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Seeds a demo merchant on startup for local development. Disabled in production via app.dev.seed=false. */
@Configuration
@ConditionalOnProperty(name = "app.dev.seed", havingValue = "true")
public class DevSeeder {

    private static final Logger log = LoggerFactory.getLogger(DevSeeder.class);
    private static final String DEV_WEBHOOK_SECRET = "dev-webhook-secret";

    @Bean
    ApplicationRunner seedMerchant(MerchantRepository merchantRepository,
                                   MerchantAccountRepository merchantAccountRepository,
                                   ApiKeyService apiKeyService, AppProperties properties) {
        return args -> {
            String apiKey = properties.dev().seedMerchantApiKey();
            String hash = apiKeyService.hash(apiKey);
            Merchant merchant = merchantRepository.findByApiKeyHashAndActiveTrue(hash)
                    .orElseGet(() -> {
                        Merchant created = merchantRepository.save(
                                new Merchant("Demo Merchant", hash, DEV_WEBHOOK_SECRET));
                        log.warn("[DEV] Seeded 'Demo Merchant'. X-Api-Key: {} | webhook secret: {}",
                                apiKey, DEV_WEBHOOK_SECRET);
                        return created;
                    });
            // Ensure the merchant has an account (covers merchants seeded before V3).
            if (merchantAccountRepository.findByMerchantId(merchant.getId()).isEmpty()) {
                merchantAccountRepository.save(new MerchantAccount(merchant.getId()));
            }
        };
    }
}
