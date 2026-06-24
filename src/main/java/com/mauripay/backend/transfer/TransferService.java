package com.mauripay.backend.transfer;

import com.mauripay.backend.common.ApiException;
import com.mauripay.backend.common.ErrorCode;
import com.mauripay.backend.ledger.LedgerTransaction;
import com.mauripay.backend.ledger.LedgerTransactionRepository;
import com.mauripay.backend.user.Account;
import com.mauripay.backend.user.AccountRepository;
import com.mauripay.backend.user.AppUser;
import com.mauripay.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransferService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final LedgerTransactionRepository ledgerRepository;

    public TransferService(UserRepository userRepository, AccountRepository accountRepository,
                           LedgerTransactionRepository ledgerRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.ledgerRepository = ledgerRepository;
    }

    /** Moves money from the current user to the user identified by phone, recording one ledger entry. */
    @Transactional
    public void transfer(UUID senderUserId, String receiverPhone, BigDecimal amount, String type) {
        AppUser receiver = userRepository.findByPhone(receiverPhone)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RECIPIENT_NOT_FOUND, "Recipient not found"));
        if (receiver.getId().equals(senderUserId)) {
            throw ApiException.badRequest(ErrorCode.SELF_TRANSFER, "Cannot transfer to yourself");
        }

        // Lock both accounts in a consistent (id-ordered) order to avoid deadlocks.
        UUID receiverUserId = receiver.getId();
        Account sender;
        Account dest;
        if (senderUserId.compareTo(receiverUserId) < 0) {
            sender = lock(senderUserId);
            dest = lock(receiverUserId);
        } else {
            dest = lock(receiverUserId);
            sender = lock(senderUserId);
        }

        if (sender.getBalance().compareTo(amount) < 0) {
            throw ApiException.unprocessable(ErrorCode.INSUFFICIENT_BALANCE, "Insufficient balance");
        }
        sender.debit(amount);
        dest.credit(amount);

        String transactionType = (type == null || type.isBlank()) ? "transfer" : type;
        ledgerRepository.save(
                new LedgerTransaction(senderUserId, receiverUserId, amount, transactionType, null));
    }

    private Account lock(UUID userId) {
        return accountRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));
    }
}
