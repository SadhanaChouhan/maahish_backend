package com.maahish.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends MaahishException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
