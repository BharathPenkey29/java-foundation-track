package com.signia.training;

public final class Q4TransientException
        extends Q4AppException {

    private final int attempt;

    public Q4TransientException(
            Q4ErrorCode errorCode,
            String message,
            int attempt,
            Throwable cause) {

        super(errorCode, message, cause);

        if (attempt < 1) {
            throw new IllegalArgumentException(
                    "attempt must be >= 1"
            );
        }

        this.attempt = attempt;
    }

    public int attempt() {
        return attempt;
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}