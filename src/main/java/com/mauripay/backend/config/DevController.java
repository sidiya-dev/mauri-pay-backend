package com.mauripay.backend.config;

import com.mauripay.backend.auth.AppUserDetails;
import com.mauripay.backend.common.ApiException;
import com.mauripay.backend.user.Account;
import com.mauripay.backend.user.AccountRepository;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/** Dev-only helper to fund an account so the pay flow can be exercised end-to-end. */
@RestController
@RequestMapping("/api/v1/dev")
@ConditionalOnProperty(name = "app.dev.seed", havingValue = "true")
public class DevController {

    public record TopUpRequest(@NotNull @DecimalMin("0.01") BigDecimal amount) {
    }

    private final AccountRepository accountRepository;

    public DevController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @PostMapping("/topup")
    @Transactional
    public Account topUp(@AuthenticationPrincipal AppUserDetails principal,
                         @RequestBody TopUpRequest request) {
        Account account = accountRepository.findByUserIdForUpdate(principal.getId())
                .orElseThrow(() -> ApiException.notFound("Account not found"));
        account.credit(request.amount());
        return account;
    }
}
