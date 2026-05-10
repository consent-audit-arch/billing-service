package com.tcc.billing_service.domain.exception;

public class UserServiceCommunicationException extends RuntimeException {
    public UserServiceCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
