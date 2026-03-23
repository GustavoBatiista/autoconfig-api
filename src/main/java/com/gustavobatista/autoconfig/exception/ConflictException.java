package com.gustavobatista.autoconfig.exception;

public class ConflictException extends ApiException {

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}