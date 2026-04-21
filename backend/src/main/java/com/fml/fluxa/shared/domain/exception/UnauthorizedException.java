package com.fml.fluxa.shared.domain.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends FluxaException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
