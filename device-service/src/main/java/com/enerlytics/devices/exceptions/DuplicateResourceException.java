package com.enerlytics.devices.exceptions;

import org.springframework.http.HttpStatus;

public final class DuplicateResourceException extends ApplicationException {

    public DuplicateResourceException(String resource, Object identifier) {
        super("%s already exists with id: %s".formatted(resource, identifier), HttpStatus.CONFLICT);
    }
}
