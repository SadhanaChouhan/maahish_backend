package com.maahish.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success;
    private String message;
    private String errorCode;
    private int status;
    private Map<String, String> fieldErrors;
    private LocalDateTime timestamp;

    public static ErrorResponse of(String message, String errorCode, int status) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
