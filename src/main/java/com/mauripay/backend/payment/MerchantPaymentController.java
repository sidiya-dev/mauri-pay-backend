package com.mauripay.backend.payment;

import com.mauripay.backend.merchant.MerchantAuthentication;
import com.mauripay.backend.payment.dto.CreatePaymentRequest;
import com.mauripay.backend.payment.dto.PaymentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Server-to-server endpoints authenticated by the {@code X-Api-Key} header. */
@RestController
@RequestMapping("/api/v1/payments")
public class MerchantPaymentController {

    private final PaymentService paymentService;

    public MerchantPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(MerchantAuthentication merchant,
                                  @Valid @RequestBody CreatePaymentRequest request) {
        return PaymentResponse.from(paymentService.create(merchant.getMerchantId(), request));
    }

    @GetMapping("/{code}/status")
    public PaymentResponse status(@PathVariable String code) {
        return PaymentResponse.from(paymentService.getByCode(code));
    }
}
