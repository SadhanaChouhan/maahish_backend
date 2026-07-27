package com.maahish.common.exception;

import org.springframework.http.HttpStatus;

public class CloudinaryException extends MaahishException {

    public CloudinaryException(String message) {
        super(message, HttpStatus.BAD_GATEWAY, "CLOUDINARY_ERROR");
    }

    public CloudinaryException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage(), HttpStatus.BAD_GATEWAY, "CLOUDINARY_ERROR");
    }
}
