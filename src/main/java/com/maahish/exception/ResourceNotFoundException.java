package com.maahish.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends MaahishException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
