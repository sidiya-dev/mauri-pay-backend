package com.mauripay.backend.auth;

import com.mauripay.backend.auth.exception.AccountLockedException;
import com.mauripay.backend.config.AppProperties;
import com.mauripay.backend.user.AppUser;
import com.mauripay.backend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Brute-force protection around login. Tracks consecutive failed attempts per user and
 * temporarily locks the account once the configured threshold is reached.
 * Lookups by phone that miss are intentionally silent so we never reveal which phones exist.
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final UserRepository userRepository;
    private final AppProperties properties;

    public LoginAttemptService(UserRepository userRepository, AppProperties properties) {
        this.userRepository = userRepository;
        this.properties = properties;
    }

    /** Rejects the attempt early if the account is currently locked. */
    @Transactional(readOnly = true)
    public void assertNotLocked(String phone) {
        userRepository.findByPhone(phone).ifPresent(user -> {
            if (user.isLocked()) {
                long minutes = (user.remainingLockSeconds() + 59) / 60;
                log.warn("Blocked login for locked account phone={} ({}m remaining)", phone, minutes);
                throw new AccountLockedException(
                        "Account locked due to too many failed attempts. Try again in "
                                + minutes + " minute(s).");
            }
        });
    }

    @Transactional
    public void onFailedLogin(String phone) {
        userRepository.findByPhone(phone).ifPresent(user -> {
            user.registerFailedAttempt(
                    properties.security().maxFailedAttempts(), properties.security().lockMinutes());
            userRepository.save(user);
            if (user.isLocked()) {
                log.warn("Account locked after repeated failures phone={}", phone);
            }
        });
    }

    @Transactional
    public void onSuccessfulLogin(String phone) {
        userRepository.findByPhone(phone).ifPresent((AppUser user) -> {
            user.resetFailedAttempts();
            userRepository.save(user);
        });
    }
}
