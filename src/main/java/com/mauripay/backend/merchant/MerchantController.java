package com.mauripay.backend.merchant;

import com.mauripay.backend.merchant.dto.MerchantProfileResponse;
import com.mauripay.backend.merchant.dto.MerchantRegisteredResponse;
import com.mauripay.backend.merchant.dto.RegisterMerchantRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    /** Public: a company creates a merchant account and receives its API key once. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public MerchantRegisteredResponse register(@Valid @RequestBody RegisterMerchantRequest request) {
        return merchantService.register(request);
    }

    /** Merchant (X-Api-Key): profile + current balance. */
    @GetMapping("/me")
    public MerchantProfileResponse me(MerchantAuthentication merchant) {
        return merchantService.profile(merchant.getMerchantId());
    }
}
