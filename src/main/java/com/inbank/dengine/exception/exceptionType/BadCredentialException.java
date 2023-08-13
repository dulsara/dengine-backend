package com.inbank.dengine.exception.exceptionType;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class BadCredentialException extends RuntimeException {
    public BadCredentialException(String message) {
        super(message);
    }
}