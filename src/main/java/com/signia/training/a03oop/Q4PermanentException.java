package com.signia.training.a03oop;

public final class Q4PermanentException
        extends Q4AppException {

    public Q4PermanentException(
            Q4ErrorCode errorCode,
            String message,
            Throwable cause) {

        super(errorCode, message, cause);
    }

    @Override
    public boolean isRetryable() {
        return false;
    }
}