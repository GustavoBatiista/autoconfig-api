package com.gustavobatista.autoconfig.exception;

public class BusinessRuleException extends ApiException {

    public BusinessRuleException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}