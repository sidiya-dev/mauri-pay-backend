package com.mauripay.backend.auth;

import com.mauripay.backend.auth.dto.RegisterRequest;
import com.mauripay.backend.auth.dto.UserResponse;
import com.mauripay.backend.common.ApiException;
import com.mauripay.backend.user.Account;
import com.mauripay.backend.user.AccountRepository;
import com.mauripay.backend.user.AppUser;
import com.mauripay.backend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, AccountRepository accountRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.phone())) {
            throw ApiException.conflict("Phone already registered");
        }
        AppUser user = userRepository.save(
                new AppUser(request.phone(), request.fullName(), passwordEncoder.encode(request.password())));
        Account account = accountRepository.save(new Account(user.getId()));
        return toResponse(user, account);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Account not found"));
        return toResponse(user, account);
    }

    private UserResponse toResponse(AppUser user, Account account) {
        return new UserResponse(user.getId(), user.getPhone(), user.getFullName(),
                account.getBalance(), account.getCurrency());
    }
}
