package com.fml.fluxa.shared.domain.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends FluxaException {

    public BusinessException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_ERROR");
    }

    public BusinessException(String message, String code) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, code);
    }
}
