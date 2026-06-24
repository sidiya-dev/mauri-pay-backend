package com.mauripay.backend.ledger;

import com.mauripay.backend.auth.AppUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Lists the current user's ledger history (money sent and received). */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final LedgerTransactionRepository ledgerRepository;

    public TransactionController(LedgerTransactionRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<TransactionResponse> list(@AuthenticationPrincipal AppUserDetails principal) {
        return ledgerRepository
                .findBySenderIdOrReceiverIdOrderByCreatedAtDesc(principal.getId(), principal.getId())
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
