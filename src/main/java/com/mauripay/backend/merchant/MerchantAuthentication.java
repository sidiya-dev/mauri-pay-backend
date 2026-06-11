package com.mauripay.backend.merchant;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

/** Authentication token representing an API-key authenticated merchant. */
public class MerchantAuthentication extends AbstractAuthenticationToken {

    private final UUID merchantId;

    public MerchantAuthentication(UUID merchantId) {
        super(List.of(new SimpleGrantedAuthority("ROLE_MERCHANT")));
        this.merchantId = merchantId;
        setAuthenticated(true);
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return merchantId;
    }
}
