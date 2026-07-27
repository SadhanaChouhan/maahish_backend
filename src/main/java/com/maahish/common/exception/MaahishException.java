package com.maahish.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MaahishException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public MaahishException(String message, HttpStatus status) {
        this(message, status, null);
    }

    public MaahishException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
