package com.fml.fluxa.shared.domain.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends FluxaException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
