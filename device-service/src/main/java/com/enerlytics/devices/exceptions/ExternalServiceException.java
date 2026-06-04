package com.enerlytics.devices.exceptions;

import org.springframework.http.HttpStatus;

public final class ExternalServiceException extends ApplicationException {

    public ExternalServiceException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_GATEWAY, cause);
    }
}
