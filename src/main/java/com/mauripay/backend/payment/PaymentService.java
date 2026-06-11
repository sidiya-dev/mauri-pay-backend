package com.mauripay.backend.payment;

import com.mauripay.backend.common.ApiException;
import com.mauripay.backend.common.PaymentCodeGenerator;
import com.mauripay.backend.config.AppProperties;
import com.mauripay.backend.ledger.LedgerTransaction;
import com.mauripay.backend.ledger.LedgerTransactionRepository;
import com.mauripay.backend.merchant.Merchant;
import com.mauripay.backend.merchant.MerchantRepository;
import com.mauripay.backend.payment.dto.CreatePaymentRequest;
import com.mauripay.backend.payment.dto.PaymentPreview;
import com.mauripay.backend.user.Account;
import com.mauripay.backend.user.AccountRepository;
import com.mauripay.backend.webhook.WebhookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRequestRepository paymentRepository;
    private final MerchantRepository merchantRepository;
    private final AccountRepository accountRepository;
    private final LedgerTransactionRepository ledgerRepository;
    private final WebhookService webhookService;
    private final PaymentCodeGenerator codeGenerator;
    private final AppProperties properties;

    public PaymentService(PaymentRequestRepository paymentRepository,
                          MerchantRepository merchantRepository, AccountRepository accountRepository,
                          LedgerTransactionRepository ledgerRepository, WebhookService webhookService,
                          PaymentCodeGenerator codeGenerator, AppProperties properties) {
        this.paymentRepository = paymentRepository;
        this.merchantRepository = merchantRepository;
        this.accountRepository = accountRepository;
        this.ledgerRepository = ledgerRepository;
        this.webhookService = webhookService;
        this.codeGenerator = codeGenerator;
        this.properties = properties;
    }

    /** Merchant creates a payment request with a fixed amount and gets a code back. */
    @Transactional
    public PaymentRequest create(UUID merchantId, CreatePaymentRequest request) {
        String currency = request.currency() == null ? "MRU" : request.currency().toUpperCase();
        Instant expiresAt = Instant.now().plus(properties.payment().codeTtlMinutes(), ChronoUnit.MINUTES);

        PaymentRequest payment = new PaymentRequest(uniqueCode(), merchantId,
                request.amount(), currency, request.orderRef(), request.callbackUrl(), expiresAt);
        return paymentRepository.save(payment);
    }

    /** Read-only preview shown to the paying user. The amount here is authoritative and uneditable. */
    @Transactional(readOnly = true)
    public PaymentPreview preview(String code) {
        PaymentRequest payment = paymentRepository.findByCode(code)
                .orElseThrow(() -> ApiException.notFound("Unknown payment code"));
        Merchant merchant = merchantRepository.findById(payment.getMerchantId()).orElseThrow();
        return new PaymentPreview(payment.getCode(), payment.getAmount(), payment.getCurrency(),
                merchant.getName(), effectiveStatus(payment));
    }

    /**
     * Pays a code from the user's balance. The amount is read from the stored request — the caller
     * never supplies it — so it cannot be edited. Idempotent: a code can only be paid once.
     */
    @Transactional
    public PaymentRequest pay(String code, UUID userId) {
        PaymentRequest payment = paymentRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> ApiException.notFound("Unknown payment code"));

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw ApiException.conflict("Payment already completed");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw ApiException.unprocessable("Payment is " + payment.getStatus());
        }
        if (payment.isExpired()) {
            payment.setStatus(PaymentStatus.EXPIRED);
            throw ApiException.unprocessable("Payment code has expired");
        }

        Account account = accountRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> ApiException.notFound("Account not found"));
        BigDecimal amount = payment.getAmount();
        if (account.getBalance().compareTo(amount) < 0) {
            throw ApiException.unprocessable("Insufficient balance");
        }

        account.debit(amount);
        ledgerRepository.save(new LedgerTransaction(userId, null, amount, "PAYMENT", payment.getId()));
        payment.markPaid(userId);

        webhookService.enqueuePaid(payment);
        return payment;
    }

    @Transactional(readOnly = true)
    public PaymentRequest getByCode(String code) {
        return paymentRepository.findByCode(code)
                .orElseThrow(() -> ApiException.notFound("Unknown payment code"));
    }

    private PaymentStatus effectiveStatus(PaymentRequest payment) {
        if (payment.getStatus() == PaymentStatus.PENDING && payment.isExpired()) {
            return PaymentStatus.EXPIRED;
        }
        return payment.getStatus();
    }

    private String uniqueCode() {
        for (int i = 0; i < 5; i++) {
            String code = codeGenerator.generate();
            if (!paymentRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Unable to generate a unique payment code");
    }
}
