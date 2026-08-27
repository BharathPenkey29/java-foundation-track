package com.signia.training;

public sealed abstract class Q4AppException
        extends Exception
        permits Q4TransientException,
        Q4PermanentException,
        Q4ThrottleException {

    private final Q4ErrorCode errorCode;

    protected Q4AppException(
            Q4ErrorCode errorCode,
            String message,
            Throwable cause) {

        super(message, cause);

        if (errorCode == null) {
            throw new IllegalArgumentException(
                    "errorCode cannot be null"
            );
        }

        this.errorCode = errorCode;
    }

    public Q4ErrorCode errorCode() {
        return errorCode;
    }

    public abstract boolean isRetryable();
}