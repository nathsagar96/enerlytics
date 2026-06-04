package com.enerlytics.devices.exceptions;

import org.springframework.http.HttpStatus;

public final class BusinessRuleException extends ApplicationException {

    public BusinessRuleException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
