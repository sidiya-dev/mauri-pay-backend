package com.mauripay.backend.merchant;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface MerchantAccountRepository extends JpaRepository<MerchantAccount, UUID> {

    Optional<MerchantAccount> findByMerchantId(UUID merchantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from MerchantAccount a where a.merchantId = :merchantId")
    Optional<MerchantAccount> findByMerchantIdForUpdate(UUID merchantId);
}
