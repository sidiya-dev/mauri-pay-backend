package com.mauripay.backend.auth.exception;

import com.mauripay.backend.common.ApiException;
import com.mauripay.backend.common.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException(String message) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, message);
    }
}
