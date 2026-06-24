package com.mauripay.backend.merchant;

import com.mauripay.backend.common.ApiException;
import com.mauripay.backend.common.ErrorCode;
import com.mauripay.backend.merchant.dto.MerchantProfileResponse;
import com.mauripay.backend.merchant.dto.MerchantRegisteredResponse;
import com.mauripay.backend.merchant.dto.RegisterMerchantRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class MerchantService {

    private static final Logger log = LoggerFactory.getLogger(MerchantService.class);
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final MerchantRepository merchantRepository;
    private final MerchantAccountRepository merchantAccountRepository;
    private final ApiKeyService apiKeyService;
    private final SecureRandom random = new SecureRandom();

    public MerchantService(MerchantRepository merchantRepository,
                           MerchantAccountRepository merchantAccountRepository,
                           ApiKeyService apiKeyService) {
        this.merchantRepository = merchantRepository;
        this.merchantAccountRepository = merchantAccountRepository;
        this.apiKeyService = apiKeyService;
    }

    /** Registers a company, returning its API key + webhook secret exactly once. */
    @Transactional
    public MerchantRegisteredResponse register(RegisterMerchantRequest request) {
        String apiKey = "mp_" + randomToken(24);
        String webhookSecret = randomToken(24);

        Merchant merchant = merchantRepository.save(new Merchant(
                request.name().trim(), apiKeyService.hash(apiKey), webhookSecret, request.callbackUrl()));
        merchantAccountRepository.save(new MerchantAccount(merchant.getId()));

        log.info("Registered merchant {} ({})", merchant.getId(), merchant.getName());
        return new MerchantRegisteredResponse(
                merchant.getId(), merchant.getName(), apiKey, webhookSecret, merchant.getCallbackUrl());
    }

    @Transactional(readOnly = true)
    public MerchantProfileResponse profile(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));
        MerchantAccount account = merchantAccountRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.MERCHANT_NOT_FOUND, "Merchant account not found"));
        return new MerchantProfileResponse(merchant.getId(), merchant.getName(),
                merchant.getCallbackUrl(), account.getBalance(), account.getCurrency());
    }

    private String randomToken(int bytes) {
        byte[] buf = new byte[bytes];
        random.nextBytes(buf);
        return URL_ENCODER.encodeToString(buf);
    }
}
