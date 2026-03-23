package com.gustavobatista.autoconfig.exception;

public class ForbiddenOperationException extends ApiException {

    public ForbiddenOperationException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }

    public ForbiddenOperationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}