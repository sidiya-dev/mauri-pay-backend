package com.mauripay.backend.merchant;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class ApiKeyService {

    private final MerchantRepository merchantRepository;

    public ApiKeyService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    /** Deterministic SHA-256 hash so a presented key can be looked up directly. */
    public String hash(String apiKey) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash API key", e);
        }
    }

    public Optional<Merchant> authenticate(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }
        return merchantRepository.findByApiKeyHashAndActiveTrue(hash(apiKey));
    }
}
