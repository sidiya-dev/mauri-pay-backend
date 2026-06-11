package com.mauripay.backend.common;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates short, human-readable payment codes using Crockford base32
 * (no I, L, O, U to avoid confusion when typed by a user).
 */
@Component
public class PaymentCodeGenerator {

    private static final char[] ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final int LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }
}
