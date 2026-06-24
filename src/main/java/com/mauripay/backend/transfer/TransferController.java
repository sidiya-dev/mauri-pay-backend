package com.mauripay.backend.transfer;

import com.mauripay.backend.auth.AppUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public Map<String, Boolean> transfer(@AuthenticationPrincipal AppUserDetails principal,
                                         @Valid @RequestBody TransferRequest request) {
        transferService.transfer(principal.getId(), request.receiverPhone(),
                request.amount(), request.transactionType());
        return Map.of("success", true);
    }
}
