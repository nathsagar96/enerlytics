package com.enerlytics.devices.exceptions;

import org.springframework.http.HttpStatus;

public abstract sealed class ApplicationException extends RuntimeException
        permits ResourceNotFoundException, DuplicateResourceException, BusinessRuleException, ExternalServiceException {

    private final HttpStatus httpStatus;

    protected ApplicationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    protected ApplicationException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
