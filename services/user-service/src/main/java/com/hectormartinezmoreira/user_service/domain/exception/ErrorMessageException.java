package com.hectormartinezmoreira.user_service.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorMessageException extends RuntimeException {
    private HttpStatus status = HttpStatus.BAD_REQUEST;

    // Default constructor: BAD_REQUEST
    public ErrorMessageException(String message) {
        super(message);
    }

    // Custom status constructor
    public ErrorMessageException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
