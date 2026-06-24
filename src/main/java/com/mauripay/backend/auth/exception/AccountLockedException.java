package com.mauripay.backend.auth.exception;

import com.mauripay.backend.common.ApiException;
import com.mauripay.backend.common.ErrorCode;
import org.springframework.http.HttpStatus;

public class AccountLockedException extends ApiException {
    public AccountLockedException(String message) {
        super(HttpStatus.LOCKED, ErrorCode.ACCOUNT_LOCKED, message);
    }
}
