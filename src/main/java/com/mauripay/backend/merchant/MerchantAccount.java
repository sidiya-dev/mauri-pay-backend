package com.mauripay.backend.merchant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "merchant_account")
public class MerchantAccount {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "merchant_id", nullable = false, unique = true)
    private UUID merchantId;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private String currency = "MRU";

    @Version
    private long version;

    protected MerchantAccount() {
    }

    public MerchantAccount(UUID merchantId) {
        this.merchantId = merchantId;
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public UUID getId() {
        return id;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }
}
