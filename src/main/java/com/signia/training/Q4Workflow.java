package com.signia.training ;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class Q4Workflow {

    private final Q4Step step;

    public Q4Workflow(Q4Step step) {

        if (step == null) {
            throw new IllegalArgumentException(
                    "step cannot be null"
            );
        }

        this.step = step;
    }

    /*
     * TIMEOUT
     *
     * SocketTimeoutException is a subclass of IOException.
     * We catch the specific exception first.
     */
    public void executeTimeout(int attempt)
            throws Q4AppException {

        try {

            step.timeout();

        } catch (SocketTimeoutException e) {

            throw new Q4TransientException(
                    Q4ErrorCode.TIMEOUT,
                    "Remote operation timed out",
                    attempt,
                    e
            );

        } catch (IOException e) {

            throw new Q4TransientException(
                    Q4ErrorCode.TIMEOUT,
                    "I/O failure while performing timeout operation",
                    attempt,
                    e
            );
        }
    }

    /*
     * CONNECTION RESET
     */
    public void executeConnectionReset(int attempt)
            throws Q4AppException {

        try {

            step.connectionReset();

        } catch (IOException e) {

            throw new Q4TransientException(
                    Q4ErrorCode.CONNECTION_RESET,
                    "Connection was reset",
                    attempt,
                    e
            );
        }
    }

    /*
     * VALIDATION FAILURE
     */
    public void executeValidationFailure()
            throws Q4AppException {

        try {

            step.validationFailure();

        } catch (IOException e) {

            throw new Q4PermanentException(
                    Q4ErrorCode.VALIDATION_FAILED,
                    "Validation failed",
                    e
            );
        }
    }

    /*
     * THROTTLE
     */
    public void executeThrottle()
            throws Q4AppException {

        try {

            step.throttle();

        } catch (IOException e) {

            throw new Q4ThrottleException(
                    Q4ErrorCode.THROTTLED,
                    "Request was throttled",
                    e
            );
        }
    }

    /*
     * SUCCESS
     */
    public void executeSuccess() {

        step.successfulOperation();
    }

    /*
     * WRONG PATTERN:
     *
     * catch (Exception e) {
     *     e.printStackTrace();
     * }
     *
     * This is too broad because this layer should only catch
     * exceptions it can meaningfully translate or handle.
     */
}