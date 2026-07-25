package com.maahish.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends MaahishException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }
}
