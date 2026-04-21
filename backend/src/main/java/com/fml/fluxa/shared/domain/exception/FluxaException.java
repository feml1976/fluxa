package com.fml.fluxa.shared.domain.exception;

import org.springframework.http.HttpStatus;

public class FluxaException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public FluxaException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode()       { return code; }
}
