package com.maahish.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends MaahishException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
