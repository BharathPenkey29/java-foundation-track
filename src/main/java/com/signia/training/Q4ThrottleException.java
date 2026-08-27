package com.signia.training;

public final class Q4ThrottleException
        extends Q4AppException {

    public Q4ThrottleException(
            Q4ErrorCode errorCode,
            String message,
            Throwable cause) {

        super(errorCode, message, cause);
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}