package com.mauripay.backend.payment;

import com.mauripay.backend.auth.AppUserDetails;
import com.mauripay.backend.payment.dto.PaymentPreview;
import com.mauripay.backend.payment.dto.PaymentResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Session-authenticated endpoints used by the paying user. */
@RestController
@RequestMapping("/api/v1/payments")
public class UserPaymentController {

    private final PaymentService paymentService;

    public UserPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Preview a code before paying. Amount is fixed by the merchant and not editable. */
    @GetMapping("/{code}")
    public PaymentPreview preview(@PathVariable String code) {
        return paymentService.preview(code);
    }

    /** Pay the code from the current user's balance. No amount is accepted in the body. */
    @PostMapping("/{code}/pay")
    public PaymentResponse pay(@PathVariable String code,
                               @AuthenticationPrincipal AppUserDetails principal) {
        return PaymentResponse.from(paymentService.pay(code, principal.getId()));
    }
}
